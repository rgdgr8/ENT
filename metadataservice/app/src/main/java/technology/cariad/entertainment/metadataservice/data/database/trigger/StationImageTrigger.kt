package technology.cariad.entertainment.metadataservice.data.database.trigger

//language=SQL
const val TRIGGER_STATION_IMAGE_AFTER_DELETE =
    """
        CREATE TRIGGER `trigger_station_image_after_delete`
        AFTER DELETE ON `station_image`
        BEGIN
            DELETE FROM image WHERE `image_id` = `OLD`.`image_id`;
        END;
    """

