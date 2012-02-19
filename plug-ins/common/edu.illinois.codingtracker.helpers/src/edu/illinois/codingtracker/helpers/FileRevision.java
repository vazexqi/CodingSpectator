/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.helpers;

import org.eclipse.core.resources.IFile;

/**
 * 
 * @author Stas Negara
 * 
 */
public class FileRevision {

	private IFile file;

	private String revision;

	private String committedRevision;

	public FileRevision(IFile file, String revision, String committedRevision) {
		this.file= file;
		this.revision= revision;
		this.committedRevision= committedRevision;
	}

	public IFile getFile() {
		return file;
	}

	public String getRevision() {
		return revision;
	}

	public String getCommittedRevision() {
		return committedRevision;
	}

	//Note for both hashCode and equals methods that all implementations of IFile also extend Resource, 
	//which implements hashCode and equals methods, so it is ok to call these methods on field 'file'.

	@Override
	public int hashCode() {
		final int prime= 31;
		int result= 1;
		result= prime * result + ((file == null) ? 0 : file.hashCode());
		result= prime * result + ((revision == null) ? 0 : revision.hashCode());
		result= prime * result + ((committedRevision == null) ? 0 : committedRevision.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileRevision other= (FileRevision)obj;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		if (revision == null) {
			if (other.revision != null)
				return false;
		} else if (!revision.equals(other.revision))
			return false;
		if (committedRevision == null) {
			if (other.committedRevision != null)
				return false;
		} else if (!committedRevision.equals(other.committedRevision))
			return false;
		return true;
	}

}
