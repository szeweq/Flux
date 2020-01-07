package szewek.mcgen.task

open class TemplateTags : AbstractTemplateTask("tags") {
    override fun outputDirName(namespace: String) = "data/forge/tags"
}