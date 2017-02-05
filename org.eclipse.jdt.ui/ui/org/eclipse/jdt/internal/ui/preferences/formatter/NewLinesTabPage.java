/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.preferences.formatter;

import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;



public class NewLinesTabPage extends FormatterTabPage {

	/**
	 * Constant array for boolean selection
	 */
	private static String[] FALSE_TRUE = {
		DefaultCodeFormatterConstants.FALSE,
		DefaultCodeFormatterConstants.TRUE
	};

    /**
     * Constant array for insert / not_insert.
     */
    private static String[] DO_NOT_INSERT_INSERT = {
        JavaCore.DO_NOT_INSERT,
        JavaCore.INSERT
    };


	private final String PREVIEW=
	createPreviewHeader(FormatterMessages.NewLinesTabPage_preview_header) +
	"public class Empty {}\n" + //$NON-NLS-1$
	"class Example {" + //$NON-NLS-1$
	"  static int [] fArray= {1, 2, 3, 4, 5 };" + //$NON-NLS-1$
	"  Listener fListener= new Listener() {" + //$NON-NLS-1$
	"  };\n" + //$NON-NLS-1$
	"  @Deprecated @Override " + //$NON-NLS-1$
	"  public void\nbar\n(@SuppressWarnings(\"unused\")\n int i)\n {\n@SuppressWarnings(\"unused\") int k;\n}\n" + //$NON-NLS-1$
	"  void foo() {" + //$NON-NLS-1$
	"    ;;" + //$NON-NLS-1$
	"    do {} while (false);" + //$NON-NLS-1$
	"    for (;;) {}" + //$NON-NLS-1$
	"  }" + //$NON-NLS-1$
	"}"+ //$NON-NLS-1$
	"\n" + //$NON-NLS-1$
	"enum MyEnum {" + //$NON-NLS-1$
	"    UNDEFINED(0) { }" + //$NON-NLS-1$
	"}" + //$NON-NLS-1$
	"enum EmptyEnum { }" +//$NON-NLS-1$
	"@interface EmptyAnnotation { }";//$NON-NLS-1$


	protected CheckboxPreference fThenStatementPref, fSimpleIfPref;

	private CompilationUnitPreview fPreview;

	public NewLinesTabPage(ModifyDialog modifyDialog, Map workingValues) {
		super(modifyDialog, workingValues);
	}

	protected void doCreatePreferences(Composite composite, int numColumns) {

		final Group newlinesGroup= createGroup(numColumns, composite, FormatterMessages.NewLinesTabPage_newlines_group_title);
		createPref(newlinesGroup, numColumns, FormatterMessages.NewLinesTabPage_newlines_group_option_empty_class_body, DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_EMPTY_TYPE_DECLARATION, DO_NOT_INSERT_INSERT);
		createPref(newlinesGroup, numColumns, FormatterMessages.NewLinesTabPage_newlines_group_option_empty_anonymous_class_body, DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_EMPTY_ANONYMOUS_TYPE_DECLARATION, DO_NOT_INSERT_INSERT);
		createPref(newlinesGroup, numColumns, FormatterMessages.NewLinesTabPage_newlines_group_option_empty_method_body, DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_EMPTY_METHOD_BODY, DO_NOT_INSERT_INSERT);
		createPref(newlinesGroup, numColumns, FormatterMessages.NewLinesTabPage_newlines_group_option_empty_block, DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_EMPTY_BLOCK, DO_NOT_INSERT_INSERT);
		createPref(newlinesGroup, numColumns, FormatterMessages.NewLinesTabPage_newlines_group_option_empty_enum_declaration, DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_EMPTY_ENUM_DECLARATION, DO_NOT_INSERT_INSERT);
		createPref(newlinesGroup, numColumns, FormatterMessages.NewLinesTabPage_newlines_group_option_empty_enum_constant, DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_EMPTY_ENUM_CONSTANT, DO_NOT_INSERT_INSERT);
		createPref(newlinesGroup, numColumns, FormatterMessages.NewLinesTabPage_newlines_group_option_empty_annotation_decl_body, DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_EMPTY_ANNOTATION_DECLARATION, DO_NOT_INSERT_INSERT);
		createPref(newlinesGroup, numColumns, FormatterMessages.NewLinesTabPage_newlines_group_option_empty_end_of_file, DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AT_END_OF_FILE_IF_MISSING, DO_NOT_INSERT_INSERT);

		final Group arrayInitializerGroup= createGroup(numColumns, composite, FormatterMessages.NewLinesTabPage_arrayInitializer_group_title);
		createPref(arrayInitializerGroup, numColumns, FormatterMessages.NewLinesTabPage_array_group_option_after_opening_brace_of_array_initializer, DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_OPENING_BRACE_IN_ARRAY_INITIALIZER, DO_NOT_INSERT_INSERT);
		createPref(arrayInitializerGroup, numColumns, FormatterMessages.NewLinesTabPage_array_group_option_before_closing_brace_of_array_initializer, DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER, DO_NOT_INSERT_INSERT);


		final Group emptyStatementsGroup= createGroup(numColumns, composite, FormatterMessages.NewLinesTabPage_empty_statement_group_title);
		createPref(emptyStatementsGroup, numColumns, FormatterMessages.NewLinesTabPage_emtpy_statement_group_option_empty_statement_on_new_line, DefaultCodeFormatterConstants.FORMATTER_PUT_EMPTY_STATEMENT_ON_NEW_LINE, FALSE_TRUE);

		final Group annotationsGroup= createGroup(numColumns, composite, FormatterMessages.NewLinesTabPage_annotations_group_title);
		createPref(annotationsGroup, numColumns, FormatterMessages.NewLinesTabPage_annotations_group_members, DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_MEMBER, DO_NOT_INSERT_INSERT);
		createPref(annotationsGroup, numColumns, FormatterMessages.NewLinesTabPage_annotations_group_paramters, DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_PARAMETER, DO_NOT_INSERT_INSERT);
		createPref(annotationsGroup, numColumns, FormatterMessages.NewLinesTabPage_annotations_group_local_variables, DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_LOCAL_VARIABLE, DO_NOT_INSERT_INSERT);
	}

	protected void initializePage() {
	    fPreview.setPreviewText(PREVIEW);
	}

    protected JavaPreview doCreateJavaPreview(Composite parent) {
        fPreview= new CompilationUnitPreview(fWorkingValues, parent);
        return fPreview;
    }

    protected void doUpdatePreview() {
    	super.doUpdatePreview();
        fPreview.update();
    }

	private CheckboxPreference createPref(Composite composite, int numColumns, String message, String key, String[] values) {
		return createCheckboxPref(composite, numColumns, message, key, values);
	}
}
