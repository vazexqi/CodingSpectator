/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.ast.transformation;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.InferredUnknownTransformationOperation;
import edu.illinois.codingtracker.operations.ast.UnknownTransformationDescriptor;
import edu.illinois.codingtracker.tests.analyzers.CSVProducingAnalyzer;


/**
 * This analyzer counts unknown transformations for each kind and orders different kinds according
 * to their popularity.
 * 
 * @author Stas Negara
 * 
 */
public class UnknownTransformationKindsAnalyzer extends CSVProducingAnalyzer {

	private final Map<Long, TransformationInstances> unknownTransformations= new HashMap<Long, TransformationInstances>();


	@Override
	protected String getTableHeader() {
		return "username,workspace ID,transformation kind ID,transformations count,timestamps\n";
	}

	@Override
	protected void checkPostprocessingPreconditions() {
		//no preconditions
	}

	@Override
	protected boolean shouldPostprocessVersionFolder(String folderName) {
		return true;
	}

	@Override
	protected String getRecordFileName() {
		return "codechanges.txt.inferred_ast_operations.with_move.with_inferred_refactorings.with_inferred_unknown_transformations";
	}

	@Override
	protected void postprocess(List<UserOperation> userOperations) {
		initialize();
		for (UserOperation userOperation : userOperations) {
			if (userOperation instanceof InferredUnknownTransformationOperation) {
				InferredUnknownTransformationOperation transformationOperation= (InferredUnknownTransformationOperation)userOperation;
				long transformationKindID= transformationOperation.getTransformationKindID();
				TransformationInstances transformationInstances= unknownTransformations.get(transformationKindID);
				if (transformationInstances == null) {
					transformationInstances= new TransformationInstances(transformationOperation.getDescriptor());
					unknownTransformations.put(transformationKindID, transformationInstances);
				}
				transformationInstances.addInstance(transformationOperation.getTime());
			}
		}
	}

	@Override
	protected void finishedProcessingAllSequences() {
		//First, sort the results according to the transformations popularity.
		TransformationInstancesComparator comparator= new TransformationInstancesComparator(unknownTransformations);
		Map<Long, TransformationInstances> sortedUnknownTransformations= new TreeMap<Long, TransformationInstances>(comparator);
		sortedUnknownTransformations.putAll(unknownTransformations);

		for (Entry<Long, TransformationInstances> entry : sortedUnknownTransformations.entrySet()) {
			Long transformationKindID= entry.getKey();
			System.out.println("\nTransformation kind ID: " + transformationKindID);
			TransformationInstances transformationInstances= entry.getValue();
			int instancesCount= transformationInstances.getCount();
			System.out.println("Transformation instances count: " + instancesCount);
			System.out.print(transformationInstances.getDescriptorAsText());
			String instanceTimestamps= transformationInstances.getTimestamps();
			System.out.println("Transformation instances: " + instanceTimestamps);
		}
	}

	private void initialize() {
		result= new StringBuffer();
	}

	@Override
	protected String getResultFilePostfix() {
		return ".unknown_transformation_statistics";
	}

	@Override
	protected boolean shouldMergeResults() {
		return true;
	}

	@Override
	protected boolean shouldOutputIndividualResults() {
		return false;
	}

	class TransformationInstances {

		private final UnknownTransformationDescriptor descriptor;

		private final List<String> instanceTimestamps= new LinkedList<String>();


		public TransformationInstances(UnknownTransformationDescriptor descriptor) {
			this.descriptor= descriptor;
		}

		public void addInstance(long timestamp) {
			instanceTimestamps.add(postprocessedFileRelativePath + ":" + timestamp);
		}

		public String getDescriptorAsText() {
			StringBuffer sb= new StringBuffer();
			descriptor.appendContent(sb);
			return sb.toString();
		}

		public int getCount() {
			return instanceTimestamps.size();
		}

		public String getTimestamps() {
			StringBuffer sb= new StringBuffer();
			for (String timestamp : instanceTimestamps) {
				sb.append(timestamp).append(", ");
			}
			if (sb.length() > 0) {
				//Remove trailing comma.
				return sb.substring(0, sb.length() - 2);
			}
			return sb.toString();
		}

	}

	class TransformationInstancesComparator implements Comparator<Long> {

		private final Map<Long, TransformationInstances> map;


		public TransformationInstancesComparator(Map<Long, TransformationInstances> map) {
			this.map= map;
		}

		@Override
		public int compare(Long transformationKindID1, Long transformationKindID2) {
			if (map.get(transformationKindID1).getCount() >= map.get(transformationKindID2).getCount()) {
				return -1;
			}
			return 1;
		}

	}

}
