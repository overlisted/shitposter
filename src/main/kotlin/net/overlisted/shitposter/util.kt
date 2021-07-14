import net.overlisted.shitposter.Shitposter
import java.io.File

fun readResource(filename: String) = Shitposter::class.java.classLoader.getResource(filename)?.readText()
fun readOrMakeFile(filename: String): String {
    val file = File(filename)

    if(!file.exists()) file.createNewFile()

    return file.readText(Charsets.UTF_8)
}
