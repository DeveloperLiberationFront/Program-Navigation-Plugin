# Program-Navigation-Plugin

Chris and Justin's Eclipse Plug-in tool

To open and run the tool:
1. Open Eclipse
- Make sure the selected workspace is /home/flower/Desktop/flower
- When Eclipse opens, make sure you're viewing the Plugin Development Perspective
2. In the Package Explorer open the dataTool folder navigate to META-INF/MANIFEST.MF
3. Right-click on MANIFEST.MF and select Run As > Eclipse Application
	- When the message pops up about errors in org.eclipse.jdt.ui, click Proceed
	4. A new Eclipse window will open with the plugin installed
	5. In the toolbar click on the icon with an 'x' inside of a white box to start Flower on the sample project

	Testing Flower:
	Our tool has many different features to help developers navigate their source code.
	* Clicking on any variable automatically invokes the tool and you will be able to see everywhere the variable is being used indicated by the blue highlighting.
	    - The tool is automatically re-invoked every time you click the mouse in the editor.
	    * If any occurrences of the selected variable are not visible on the screen, a link will be provided in the top or bottom breadcrumbs displaying the line number where the variable is located based on your current position.
	        - The top box will display links to occurrences higher in the code outside of the current view.
		    - The bottom box will display links to occurrences lower in the code.
		    * In addition to line numbers, the top and bottom boxes also display links to methods where the selected variable is modified or referenced. 
		        - The breadcrumb in the top of the editor will link to methods where the selected variable is passed in as a parameter.
			    - The breadcrumb at the bottom of the editor will display links to methods that use the current variable as a parameter.
			    * When variables used as parameters are selected, links will also appear in the editor to link directly to an instance where the method is called if the variable is a formal parameter passed in or to the declaration of the method where the argument is used. The tool will automatically highlight the new variable referencing the data from the previous search.
			        - A Source Not Found error will be thrown if you click on a link in the breadcrumbs or the editor that is outside the scope of the project.

				Log In:
				- If needed, the username and password for this virtual machine are both "flower".

