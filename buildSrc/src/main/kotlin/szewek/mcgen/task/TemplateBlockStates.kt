package szewek.mcgen.task

open class TemplateBlockStates : AbstractTemplateTask("blockstates") {
    override fun outputDirName(namespace: String) = "assets/$namespace/blockstates"
}