plugins {
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.asciidoctor.convert) apply false
    alias(libs.plugins.asciidoctor.pdf) apply false
    alias(libs.plugins.cyclonedx) apply false
}