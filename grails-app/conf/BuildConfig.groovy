grails.servlet.version = "3.0" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.7
grails.project.source.level = 1.7
//grails.project.war.file = "target/${appName}-${appVersion}.war"

// uncomment (and adjust settings) to fork the JVM to isolate classpaths
//grails.project.fork = [
//   run: [maxMemory:1024, minMemory:64, debug:false, maxPerm:256]
//]

forkConfig = [maxMemory: 2048, minMemory: 128, debug: false, maxPerm: 512]
debugConfig = [maxMemory: 2048, minMemory: 128, debug: true, maxPerm: 512]
grails.project.fork = [
   test: forkConfig, // configure settings for the test-app JVM
   run: forkConfig, // configure settings for the run-app JVM
   war: forkConfig, // configure settings for the run-war JVM
   console: debugConfig // configure settings for the Swing console JVM
]

grails.project.dependency.resolver = "maven"
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // specify dependency exclusions here; for example, uncomment this to disable ehcache:
        // excludes 'ehcache'
		excludes 'xercesImpl'
    }
    log "error" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve
    legacyResolve false // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility

    repositories {
        inherits true // Whether to inherit repository definitions from plugins

        grailsPlugins()
        grailsHome()
        grailsCentral()

        mavenLocal()
        mavenCentral()

        // uncomment these (or add new ones) to enable remote dependency resolution from public Maven repositories
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
    }

    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes e.g.
//		runtime: 'postgresql:postgresql:74.216.jdbc3'
         runtime 'mysql:mysql-connector-java:5.1.28'
//		 runtime  'org.postgresql:postgresql:9.3-1100-jdbc41'
		 
		
		
		compile (group:'org.apache.poi', name:'poi', version:'3.8')
		//xlxs file support
		compile (group:'org.apache.poi', name:'poi-ooxml', version:'3.8'){
			excludes 'xmlbeans'
		}
    }

    plugins {
        runtime ":resources:1.2.1"
		compile ":joda-time:1.4"

		compile ":excel-import:1.0.0"
		compile ":excel-export:0.2.0"
        // Uncomment these (or add new ones) to enable additional resources capabilities
        //runtime ":zipped-resources:1.0"
        //runtime ":cached-resources:1.0"
        //runtime ":yui-minify-resources:0.1.5"
//		compile ":postgresql-extensions:0.6.1"
		compile ':scaffolding:1.0.0'
		compile ":spring-security-core:1.2.7.3"
		compile ":spring-security-acl:1.1.1"
		compile ":spring-security-appinfo:1.0"
		compile ":spring-security-ui:0.2"
		compile ":mail:1.0.1"
		compile ":jquery-ui:1.8.24" //for spring security ( I am using 1.10.x in the app)
		runtime ":jquery:1.8.3"  //for spring security ( I am using 2.x in the app)
		compile ":famfamfam:1.0.1"
        build ':tomcat:7.0.50'
		runtime ':hibernate:3.6.10.7'

        runtime ":database-migration:1.3.8"

        compile ':cache:1.1.1'
    }
}