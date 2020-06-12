# Solr query analyser
Solr query analyzer for re-ranking based on the occurrence of certain terms in the query.

## Getting Started
### Prerequisites
* [Solr 8](https://lucene.apache.org/solr/) running in standalone or [SolrCloud](https://lucene.apache.org/solr/guide/8_5/solrcloud.html) mode.
* A configured Solr core.

### Basic Installation
Download the `queryanalyser-all-1.0.jar` jar file from [GitHub Packages](https://github.com/searchgears/queryanalysis/packages/261935).

Then copy the library jar file to the `lib` directory of the core, where you want to use the query analyser plugin:
```
cp queryanalyser-all-1.0.jar <solr_home>/<core_name>/lib
```

## Using the Query Analyser
Once the plugin has been installed, it can be configured through a custom `queryanalysis.yml` configuration file,
which needs to be created in the `<solr_home>/<core_name>/conf` directory.

### Rule matching configuration
The `queryanalyis.yml` configuration consists of 2 main building blocks, the `rules` and the `matchers` section.
The following example shows a minimal configuration for a single `author` matcher:

```
rules:
  - matchers:
      - author
    params:
      qf: author^1000

matchers:
  author:
    dictionary: author.dic
```
 
The `rules` section in the configuration file defines a list of rules. Each rule entry consists of a `matchers` list, 
which references matchers from the `matchers` section and a `params` map where additional query parameters (for re-ranking) can be configured.

The `matchers` section contains mappings to external dictionary files, which are also put in the solr core config directory.    

### Matcher dictionaries

The matcher dictionaries are simple text files, containing the terms, which should be matched in the query.
A simple author dictionary would just contain the author names:

```
Rick Riordan
Jostein Gaarder
Michael McCandless
```
 
### Load configuration
 
 After making changes to the `queryanalysis.yml` or any matcher dictionary, the Solr core needs to be [reloaded](https://lucene.apache.org/solr/guide/8_5/coreadmin-api.html#coreadmin-reload)
 or the Solr server restarted, to apply the configuration changes.
  

## Docker Example
To build a custom standalone Solr docker image with the queryanalysis plugin installed (e.g. for local integration testing), just run the `docker` gradle task:
```
./gradlew docker
```

Within the docker folder a  [docker-compose](https://docs.docker.com/compose/)  can be used to run the previously built custom Solr container.
```
cd docker
docker-compose up -d
```

To feed some example documents the [Solr Post Tool](https://lucene.apache.org/solr/guide/8_5/post-tool.html) can be used from another docker container:

```
docker run --rm -v "$PWD/data/books.json:/tmp/books.json" --network=host solr:8 post -c books /tmp/books.json
```

## Changelog
See [Releases](https://github.com/searchgears/queryanalysis/releases).

## License
Apache License 2.0
