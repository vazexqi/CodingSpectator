/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.recording.ast.inferencing;


/**
 * A helper class that keeps deltas required to match nodes in the old and the new ASTs.
 * 
 * @author Stas Negara
 * 
 */
public class MatchDelta {

	public int outlierDelta= 0; //Shift of the start position of an AST node.

	public int coveringDelta= 0; //Shift of the length of an AST node.

}
