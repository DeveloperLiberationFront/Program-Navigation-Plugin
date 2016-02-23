package edu.pdx.cs.multiview.database;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IObjectActionDelegate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.pdx.cs.multiview.jdt.util.JDTUtils;

public abstract class XMLActionDelegate extends DatabaseActionDelegateWithFiles implements IObjectActionDelegate {

	protected abstract void putIntoDatabase(List<IFile> files);

	@Override
	protected boolean fileOfInterest(IFile file) {
		return file.getFileExtension()!=null && 
			file.getFileExtension().equals("xml");
	}
	
	public void run(IAction action) {
		
		putIntoDatabase(getFileList());
	}

	protected NodeList getInteractionEventXML(DocumentBuilder docBuilder, IFile file) throws CoreException, IOException, SAXException {

		InputStream contents = null;
		ByteArrayInputStream stream = null;
		try {
			contents = file.getContents();
			//TODO: sometimes the stream contains invalid characters - purge them!
			String temp = JDTUtils.getContents(contents);
			stream = new ByteArrayInputStream((temp).getBytes());
			
			Document doc = docBuilder.parse(new InputSource(new InputStreamReader(stream)));
			Node root = doc.getChildNodes().item(0);

			return root.getChildNodes();
		} finally {
			try {
				stream.close();
				contents.close();
			} catch (Exception ignore) {}			
		}
	}
}