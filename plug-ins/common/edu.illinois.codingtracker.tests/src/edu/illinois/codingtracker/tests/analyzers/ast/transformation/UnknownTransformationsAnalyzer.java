/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.ast.transformation;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import edu.illinois.codingtracker.helpers.Configuration;
import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.InferredUnknownTransformationOperation;
import edu.illinois.codingtracker.operations.ast.UnknownTransformationDescriptor;
import edu.illinois.codingtracker.tests.analyzers.CSVProducingAnalyzer;


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
		writeFile(transformationKindsFile, getTransformationKindsAsText());
		writeFile(atomicTransformationsFile, getAtomicTransformationsAsText());
		UnknownTransformationMiner.mine();
		UnknownTransformationMiner.printState();
	}

	private void writeFile(File file, StringBuffer content) {
		try {
			ResourceHelper.ensureFileExists(file);
		} catch (IOException e) {
			throw new RuntimeException("Could not create a file for writing!", e);
		}
		try {
			ResourceHelper.writeFileContent(file, content, false);
		} catch (IOException e) {
			throw new RuntimeException("Could not write to the file!", e);
		}
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

	class ItemBlock {

		private static final long MAX_BLOCK_SIZE= 5 * 60 * 1000; //5 mins in milliseconds.

		private final long startTimestamp;

		private final boolean isFirst;

		private final TreeMap<Long, Item> items= new TreeMap<Long, Item>();


		public ItemBlock(long startTimestamp, boolean isFirst) {
			this.startTimestamp= startTimestamp;
			this.isFirst= isFirst;
		}

		public boolean isFirst() {
			return isFirst;
		}

		public TreeMap<Long, Item> getItems() {
			return items;
		}

		public boolean canBePartOfBlock(InferredUnknownTransformationOperation operation) {
			return Math.abs(operation.getTime() - startTimestamp) <= MAX_BLOCK_SIZE;
		}

		public void addToBlock(InferredUnknownTransformationOperation operation) {
			if (!canBePartOfBlock(operation)) {
				throw new RuntimeException("Tried to add operation that can not be part of the block!");
			}
			items.put(operation.getTransformationID(), new LongItem(operation.getTransformationKindID()));
		}

	}

	class OperationFilePair {

		public final InferredUnknownTransformationOperation operation;

		public final String filePath;

		public OperationFilePair(InferredUnknownTransformationOperation operation, String filePath) {
			this.operation= operation;
			this.filePath= filePath;
		}
	}

}
