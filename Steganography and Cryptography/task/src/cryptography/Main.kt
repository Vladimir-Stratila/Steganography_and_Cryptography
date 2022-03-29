package cryptography
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileNotFoundException
import javax.imageio.IIOException
import javax.imageio.ImageIO


fun main() {
    var input: String
    do {
        println("Task (hide, show, exit):")
        input = readln()
        when (input) {
            "hide" -> hide()
            "show" -> show()
            "exit" -> println("Bye!")
            else -> println("Wrong task: $input")
        }
    } while (input != "exit")
}

fun hide() {
    println("Input image file:")
    val inputFilename = readln()
    val inputFile = File(inputFilename)
    println("Output image file:")
    val outputFilename = readln()
    val outputFile = File(outputFilename)
    println("Message to hide:")
    val message = readln()
    try {
        val image: BufferedImage = ImageIO.read(inputFile)
        var bytesOfMessage = message.encodeToByteArray()
        bytesOfMessage = encryptMessage(bytesOfMessage)
        bytesOfMessage = addStopBytes(bytesOfMessage)
        val bitsArray = bytesToBits(bytesOfMessage)
        if (image.width * image.height > bytesOfMessage.size * 8) {
            println("Input image: $inputFilename")
            println("Output image: $outputFilename")
            setBitsInBlue(image, bitsArray)
            saveImage(image, outputFile)
            println("Message saved in $outputFilename image.")
        } else {
            println("The input image is not large enough to hold this message.")
        }
    } catch (e: FileNotFoundException) {
        println("Can't read input file!")
    } catch (ioe: IIOException) {
        println("Can't read input file!")
    }
}

fun show() {
    println("Input image file:")
    val inputFilename = readln()
    val inputFile = File(inputFilename)
    try {
        val image: BufferedImage = ImageIO.read(inputFile)
        val bitsArray = getBitsInBlue(image)
        var bytesArray = bitsToBytes(bitsArray).toByteArray()
        bytesArray = encryptMessage(bytesArray)
        val message = bytesArray.toString(Charsets.UTF_8)
        println("Message: $message")
    } catch (e: FileNotFoundException) {
        println("Can't read input file!")
    } catch (ioe: IIOException) {
        println("Can't read input file!")
    }
}

fun saveImage(image: BufferedImage, imageFile: File) {
    ImageIO.write(image, "png", imageFile)
}

fun changeColors(image: BufferedImage): BufferedImage {
    for (x in 0 until image.width) {
        for (y in 0 until image.height) {
            val color = Color(image.getRGB(x, y))
            val newRgb = Color(
                setLeastSignificantBitToOne(color.red),
                setLeastSignificantBitToOne(color.green),
                setLeastSignificantBitToOne(color.blue)
            ).rgb
            image.setRGB(x, y, newRgb)
        }
    }
    return image
}

fun setLeastSignificantBitToOne (pixel: Int): Int {
    return if (pixel and 1 == 0) pixel + 1 else pixel
}

fun setLeastSignificantBit (pixel: Int, bit: Boolean): Int {
    return pixel.and(254).or(if (bit) 1 else 0)
}

fun addStopBytes(array: ByteArray): ByteArray{
    return array + 0 + 0 + 3
}

fun setBitsInBlue(image: BufferedImage, bitsArray: ArrayList<Boolean>): BufferedImage {
    var x = 0
    var y = 0
    for (i in bitsArray.indices) {
        x = i % image.width
        y = i / image.width
        val color = Color(image.getRGB(x, y))
        val newRgb = Color(
            color.red,
            color.green,
            setLeastSignificantBit(color.blue, bitsArray[i])
        ).rgb
        image.setRGB(x, y, newRgb)
    }
    return image
}

fun getBitsInBlue(image: BufferedImage): ArrayList<Boolean> {
    val bitsArray = arrayListOf<Boolean>()
    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            val color = Color(image.getRGB(x, y))
            bitsArray.add(color.blue % 2 != 0)
        }
    }
    return bitsArray
}

fun bytesToBits(byteArray: ByteArray): ArrayList<Boolean> {
    val bitsArray = ArrayList<Boolean>()
    for (b in byteArray) {
        var byte: Byte = b
        var bitSet = ArrayList<Boolean>()
        for (i in 1..8) {
            bitSet.add(byte % 2 != 0)
            byte = (byte / 2).toByte()
        }
        bitSet.reverse()
        bitsArray.addAll(bitSet)
    }
    return bitsArray
}

fun bitsToBytes(bitsArray: ArrayList<Boolean>): ArrayList<Byte> {
    val bytesArray = arrayListOf<Byte>()
    var byte: Byte
    for (i in bitsArray.indices step 8) {
        byte = (bitToByte(bitsArray[i]) * 128 +
                bitToByte(bitsArray[i + 1]) * 64 +
                bitToByte(bitsArray[i + 2]) * 32 +
                bitToByte(bitsArray[i + 3]) * 16 +
                bitToByte(bitsArray[i + 4]) * 8 +
                bitToByte(bitsArray[i + 5]) * 4 +
                bitToByte(bitsArray[i + 6]) * 2 +
                bitToByte(bitsArray[i + 7])).toByte()
        bytesArray.add(byte)
        if (bytesArray[bytesArray.size - 1].toInt() == 3 &&
            bytesArray[bytesArray.size - 2].toInt() == 0 &&
            bytesArray[bytesArray.size - 3].toInt() == 0) break
    }
    bytesArray.removeLast()
    bytesArray.removeLast()
    bytesArray.removeLast()
    return bytesArray
}

fun bitToByte(bit: Boolean): Byte {
    return if (bit) 1 else 0
}

fun encryptMessage(bytes: ByteArray): ByteArray{
    val bytesOfMessage = bytes.clone()
    println("Password:")
    var password = readln().toByteArray()
    for (i in bytesOfMessage.indices) {
        bytesOfMessage[i] = (bytesOfMessage[i].toInt() xor password[i % password.size].toInt()).toByte()
    }
    return bytesOfMessage
}