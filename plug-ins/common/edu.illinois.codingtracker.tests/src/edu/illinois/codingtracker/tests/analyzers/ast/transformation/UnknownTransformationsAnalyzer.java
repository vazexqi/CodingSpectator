/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.ast.transformation;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import edu.illinois.codingtracker.helpers.Configuration;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.InferredUnknownTransformationOperation;
import edu.illinois.codingtracker.operations.ast.UnknownTransformationDescriptor;
import edu.illinois.codingtracker.tests.analyzers.CSVProducingAnalyzer;
import edu.illinois.codingtracker.tests.analyzers.ast.transformation.helpers.OperationFilePair;


/**
 * This analyzer is the entry point into mining unknown transformations as sets of atomic
 * transformations - abstracted AST node operations.
 * 
 * @author Stas Negara
 * 
 */
public class UnknownTransformationsAnalyzer extends CSVProducingAnalyzer {

	private File transformationKindsFile= new File(Configuration.postprocessorRootFolderName, "transformationKinds.txt");

	private File atomicTransformationsFile= new File(Configuration.postprocessorRootFolderName, "atomicTransformations.txt");

	private File miningResultsFolder= new File(Configuration.postprocessorRootFolderName, "MiningResults");

	private final Map<Long, UnknownTransformationDescriptor> transformationKinds= new TreeMap<Long, UnknownTransformationDescriptor>();

	private final Map<Long, OperationFilePair> atomicTransformations= new TreeMap<Long, OperationFilePair>();


	@Override
	protected String getTableHeader() {
		return "";
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
		ItemBlock currentBlock= null;
		for (UserOperation userOperation : userOperations) {
			if (userOperation instanceof InferredUnknownTransformationOperation) {
				InferredUnknownTransformationOperation transformationOperation= (InferredUnknownTransformationOperation)userOperation;
				OperationFilePair pair= new OperationFilePair(transformationOperation, postprocessedFileRelativePath);
				atomicTransformations.put(transformationOperation.getTransformationID(), pair);
				long transformationKindID= transformationOperation.getTransformationKindID();
				if (transformationKinds.get(transformationKindID) == null) {
					transformationKinds.put(transformationKindID, transformationOperation.getDescriptor());
				}

				if (currentBlock == null) {
					currentBlock= new ItemBlock(transformationOperation.getTime(), true);
				}
				if (!currentBlock.canBePartOfBlock(transformationOperation)) {
					UnknownTransformationMiner.addItemToTransactions(currentBlock.getItems(), currentBlock.isFirst(), false);
					currentBlock= new ItemBlock(transformationOperation.getTime(), false);
				}
				currentBlock.addToBlock(transformationOperation);
			}
		}
		if (currentBlock != null) {
			UnknownTransformationMiner.addItemToTransactions(currentBlock.getItems(), currentBlock.isFirst(), true);
		}
	}

	@Override
	protected void finishedProcessingAllSequences() {
		writeToFile(transformationKindsFile, getTransformationKindsAsText(), false);
		writeToFile(atomicTransformationsFile, getAtomicTransformationsAsText(), false);
		UnknownTransformationMiner.mine();
		UnknownTransformationMiner.writeResultsToFolder(miningResultsFolder);
	}

	private StringBuffer getTransformationKindsAsText() {
		StringBuffer sb= new StringBuffer();
		sb.append("ID,OperationKind,AffectedNodeType,AbstractdNodeContent,ExampleNodeContent\n");
		for (Entry<Long, UnknownTransformationDescriptor> entry : transformationKinds.entrySet()) {
			UnknownTransformationDescriptor descriptor= entry.getValue();
			sb.append(entry.getKey()).append(",").append(descriptor.getOperationKind()).append(",");
			sb.append(descriptor.getAffectedNodeType()).append(",").append(descriptor.getAbstractedNodeContent());
			sb.append(",").append(descriptor.getAffectedNodeContent()).append("\n");
		}
		return sb;
	}

	private StringBuffer getAtomicTransformationsAsText() {
		StringBuffer sb= new StringBuffer();
		sb.append("AtomicTransformationID,TransformationKindID,Timestamp,RelativeFilePath\n");
		for (Entry<Long, OperationFilePair> entry : atomicTransformations.entrySet()) {
			InferredUnknownTransformationOperation operation= entry.getValue().operation;
			sb.append(entry.getKey()).append(",").append(operation.getTransformationKindID()).append(",");
			sb.append(operation.getTime()).append(",").append(entry.getValue().filePath).append("\n");
		}
		return sb;
	}

	private void initialize() {
		result= new StringBuffer();
	}

	@Override
	protected String getResultFilePostfix() {
		return ".mining_results";
	}

	@Override
	protected boolean shouldMergeResults() {
		return true;
	}

	@Override
	protected boolean shouldOutputIndividualResults() {
		return false;
	}

}
