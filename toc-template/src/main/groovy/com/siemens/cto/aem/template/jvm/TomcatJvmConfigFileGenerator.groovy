package com.siemens.cto.aem.template.jvm
/**
 * Wrapper that contains methods that generates configuration files for Apache Tomcat.
 *
 * Created by Z003BPEJ on 6/20/14.
 */
public class TomcatJvmConfigFileGenerator {

//    private TomcatJvmConfigFileGenerator() {}
//
//    /**
//     * Generate server.xml content
//     * @param templateFileName the template file name
//     * @param jvm the jvm for which to generate the server.xml
//     * @return generated server.xml content
//     */
//    public static String getServerXmlFromFile(final String templateFileName,
//                                              final Jvm jvm
//    ) {
//        final binding = [webServerName: jvm.getHostName(),
//                         jvms         : [jvm],
//                         comments     : "",
//                         catalina     : [base: "."]
//        ]
//        return bindDataToTemplate(binding, templateFileName).toString()
//    }
//
//    static String getJvmConfigFromText(String serverXmlTemplateText, Jvm currentJvm, List<Jvm> allJvms) {
//        final binding = [
//                jvm: currentJvm,
//                jvms: allJvms
//        ]
//        return bindDataToTemplateText(binding, serverXmlTemplateText);
//    }
}