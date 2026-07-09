package com.nuance.cobra.doc;

import com.nuance.plugin.v1.shareable.IErrorCode;
import org.apache.commons.lang3.text.StrBuilder;


import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class ErrorsDocGenerator extends DocGenerator {

    private Class<? extends Enum<? extends IErrorCode>>[] errorEnumClasses;
    private String[] sectionsHeaders;
    private String docTitle;
    private String docFilename;
    private boolean internal;

    /**
     * Constructor
     * @param errorEnumClasses Enum with the config properties
     *
     */
    public ErrorsDocGenerator(String docTitle, String docFilename, String[] sectionHeaders,
                              Class<? extends Enum<? extends IErrorCode>>... errorEnumClasses) {
        this(docTitle, docFilename, sectionHeaders, false, errorEnumClasses);
    }

    public ErrorsDocGenerator(String docTitle, String docFilename, String[] sectionHeaders, boolean internal,
                              Class<? extends Enum<? extends IErrorCode>>... errorEnumClasses) {
        if (sectionHeaders.length != errorEnumClasses.length) {
            throw new IllegalArgumentException("The number of sectionHeaders does not match the number of errorEnumClass");
        }

        this.docTitle = docTitle;
        this.docFilename = docFilename;
        this.errorEnumClasses = errorEnumClasses;
        this.sectionsHeaders = sectionHeaders;
        this.internal = internal;
    }



    public void generateErrorSection(String sectionHeader,
                                     Class<? extends Enum<? extends IErrorCode>> errorEnumClass,
                                     StrBuilder outputStr) throws IOException, DocGeneratorException {

        this.generateErrorSectionHeader(sectionHeader, outputStr);

        IErrorCode[] errorCodes = getErrorCodes(errorEnumClass);

        for (IErrorCode errorCode :  errorCodes) {
            outputStr.append("| `").append(errorCode.getValue()).append("`");
            outputStr.append("| ").append(errorCode.getMessage());
            if(errorCode.getDocumentation() == null || errorCode.getDocumentation().isEmpty()) {
                outputStr.append("| ").append(errorCode.getMessage());
            } else {
                outputStr.append("| ").append(errorCode.getDocumentation());
            }
            if(internal) {
                outputStr.append("| ").append(errorCode.getInternalErrorMessage());
                outputStr.append("| ").append(errorCode.getHttpStatusCode());
                outputStr.append("| ").append(errorCode.getNOCRecommendation());
                outputStr.append("| ").append(errorCode.getSeverity());
                outputStr.append("| ").append(errorCode.getAreaOfIssue());
                outputStr.append("| ").append(errorCode.getTroubleshoot());
                outputStr.append("| ").append(errorCode.isPotentialSecurityThreat());
            }
            outputStr.append("|").appendNewLine();
        }
    }



    private void generateErrorSectionHeader(@Nullable String sectionHeader, StrBuilder outputStr)
    {
        if (sectionHeader != null) {
            outputStr.appendNewLine().append("## ").append(sectionHeader).appendNewLine();
        }

        outputStr.append("| Error code  | Message                               | Description                           |").append(internal ? " Internal message      | HTTP equivalent code  | NOC recommendation  | Severity  | Area  | Troubleshoot             | Potential security threat |" : "").appendNewLine();
        outputStr.append("|-------------|---------------------------------------|---------------------------------------|").append(internal ? "-----------------------|-----------------------|---------------------|-----------|-------|--------------------------|---------------------------|" : "").appendNewLine();
    }

    @Override
    protected void generateDocs() throws Exception {

        StrBuilder outputStr = new StrBuilder();
        generateMarkdownHeader(docTitle, outputStr);

        for (int i = 0; i < sectionsHeaders.length; ++i) {
            generateErrorSection(sectionsHeaders[i], errorEnumClasses[i], outputStr);
        }

        createMarkdownFile(docFilename, outputStr);
    }

    public static void generateErrorCsv(Class<? extends Enum<? extends IErrorCode>> errorEnumClass, char separator,
                                 StrBuilder outputStr) throws DocGeneratorException {

        IErrorCode[] errorCodes = getErrorCodes(errorEnumClass);

        outputStr.append("code").append(separator)
                .append("HTTP equiv")
                .append("name").append(separator)
                .append("documentation")
                .append("\n");
        ;

        for (IErrorCode errorCode :  errorCodes) {

            String errorDocumentation = errorCode.getDocumentation();
            if (errorDocumentation == null) {
                errorDocumentation = errorCode.getMessage();
            }

            outputStr.append(errorCode.getValue()).append(separator)
                    .append(errorCode.getHttpStatusCode()).append(separator)
                    .append(errorCode.getEnumValueName()).append(separator)
                    .append(errorDocumentation).append("\n");
        }
    }

    private static IErrorCode[] getErrorCodes(Class<? extends Enum<? extends IErrorCode>> errorEnumClass)
            throws DocGeneratorException {
        try {
            Method method = errorEnumClass.getDeclaredMethod("values");
            Object obj = method.invoke(null);
            return (IErrorCode[]) obj;

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new DocGeneratorException("Error while retrieving error definitions", e);
        }
    }
}
