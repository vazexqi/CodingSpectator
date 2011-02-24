/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.tests;

import java.io.File;
import java.util.List;

import org.junit.Test;

import edu.illinois.codingspectator.codingtracker.helpers.FileHelper;
import edu.illinois.codingspectator.codingtracker.operations.OperationDeserializer;
import edu.illinois.codingspectator.codingtracker.operations.UserOperation;

/**
 * 
 * @author Stas Negara
 * 
 *         TODO: Implement this test case
 */
public class TestRecorderReplayer extends TestCodingTracker {

	private static final File operationsRecordFile= new File("test-files/02/codechanges.txt");

	@Test
	public void shouldReplayAndRecord() {
		replayAllOperations();
	}

	private void replayAllOperations() {
		String operationsRecord= FileHelper.getFileContent(operationsRecordFile);
		final List<UserOperation> userOperations= OperationDeserializer.getUserOperations(operationsRecord);
		//FIXME: Remove @Override from Test2.java because otherwise it can not be refactored (i.e. no changes to the code).
//		int count= 0;
//		for (final UserOperation userOperation : userOperations) {
//			Display.getDefault().syncExec(new Runnable() {
//				@Override
//				public void run() {
//					try {
//						userOperation.replay();
//					} catch (Exception e) {
//						throw new RuntimeException("Failed to replay operation: " + userOperation, e);
//					}
//				}
//			});
//			try {
//				Thread.sleep(500);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			count++;
//			if (count > 116) {
//				System.out.println("count: " + count);
//			}
//		}
	}

}
