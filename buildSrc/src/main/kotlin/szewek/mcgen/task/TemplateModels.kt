package szewek.mcgen.task

open class TemplateModels : AbstractTemplateTask("models") {
    override fun outputDirName(namespace: String) = "assets/$namespace/models"
}