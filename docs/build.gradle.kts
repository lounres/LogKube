plugins {
    alias(versions.plugins.dokka)
}

dokka {
    moduleName = "LogKube"
    dokkaPublications.html {
    
    }
    
    // DOKKA-3885
    dokkaGeneratorIsolation = ClassLoaderIsolation()
}