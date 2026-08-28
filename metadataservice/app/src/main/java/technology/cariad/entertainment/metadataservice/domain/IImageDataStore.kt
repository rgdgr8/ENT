package technology.cariad.entertainment.metadataservice.domain

/**
 * Interface for a data store managing a collection of image files.
 */
interface IImageDataStore {

    /**
     * Filesystem path to the folder managed by this data store.
     * It is discouraged but not enforced to create multiple data stores sharing the same [basePath]
     */
    val basePath: String

    /**
     * Saves the image [data] in a file with the given [fileName].
     * Names of files stored within a data store have to be unique.
     * Attempting to overwrite an existing image is not allowed.
     * @param data Encoded image data.
     * @param fileName File name including the file extension.
     * @return A uri string pointing to the newly created file or null in case of an error.
     */
    fun saveImage(data: ByteArray, fileName: String): String?

    /**
     * Creates a uri string to the image referred to by [fileName].
     * @param fileName The name of the image file.
     * @return A uri string pointing to the image or null if an image with this name doesn't exists.
     */
    fun loadImageUri(fileName: String): String?

    /**
     * Deletes the image with the given [fileName].
     * If the image file doesn't exist, no further action is taken.
     * @param fileName The name of the image file.
     * @return true if successful, otherwise false
     */
    fun deleteImage(fileName: String): Boolean

    /**
     * Checks cache folder and count number of image files located there.
     * @return number of image files in the cache folder, 0 on error
     */
    fun getImageFilesCount(): Int

    /**
     * Compares provided list of file names from Database to files located in cache folder
     * and deletes files that are not present in Database.
     * @param fileNamesInDb List of file names stored in the Database.
     * @param fileCountDifference Number of files that do not have entry in Database.
     */
    fun pruneImageFiles(fileNamesInDb: List<String>, fileCountDifference: Int)
}

