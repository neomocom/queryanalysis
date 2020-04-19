package com.searchgears.queryanalysis.component;

import java.net.URL;


public class SolrResources {

    private static URL resource = SolrResources.class.getClassLoader().getResource("solr");


    public static String getPathToSolrCores() {
        return resource.getPath();
    }


    public static String getPathToCoreConfig(final String coreName) {
        return getConfigDirForCore(coreName) + "solrconfig.xml";
    }


    public static String getPathToCoreSchema(final String coreName) {
        return getConfigDirForCore(coreName) + "schema.xml";
    }


    private static String getConfigDirForCore(final String coreName) {
        return getPathToSolrCores() + "/" + coreName + "/conf/";
    }

}
