package technology.cariad.entertainment.metadataservice.data.database.trigger

//language=SQL
const val TRIGGER_COVER_ART_LOOKUP_ARTIST_AFTER_DELETE =
    """
        CREATE TRIGGER `trigger_cover_art_lookup_artist_after_delete`
        AFTER DELETE ON `cover_art_lookup`
        WHEN NOT EXISTS (SELECT 1 FROM `cover_art_lookup` WHERE `artist_id` = `OLD`.`artist_id`)
        BEGIN
            DELETE FROM `artist` WHERE `artist_id` = `OLD`.`artist_id`;
        END;
    """

//language=SQL
const val TRIGGER_COVER_ART_LOOKUP_ALBUM_AFTER_DELETE =
    """
        CREATE TRIGGER `trigger_cover_art_lookup_album_after_delete`
        AFTER DELETE ON `cover_art_lookup`
        WHEN NOT EXISTS (SELECT 1 FROM `cover_art_lookup` WHERE `album_id` = `OLD`.`album_id`)
        BEGIN
            DELETE FROM `album` WHERE `album_id` = `OLD`.`album_id`;
        END;
    """

//language=SQL
const val TRIGGER_COVER_ART_LOOKUP_IMAGE_AFTER_DELETE =
    """
        CREATE TRIGGER `trigger_cover_art_lookup_image_after_delete`
        AFTER DELETE ON `cover_art_lookup` 
        BEGIN
            DELETE FROM image WHERE `image_id` = `OLD`.`image_id`;
        END;
    """

//language=SQL
const val TRIGGER_COVER_ART_LOOKUP_LIFE_CYCLE_DATA_AFTER_DELETE =
    """
        CREATE TRIGGER `trigger_cover_art_lookup_life_cycle_data_after_delete`
        AFTER DELETE ON `cover_art_lookup` 
        BEGIN
            DELETE FROM `life_cycle_data` WHERE `life_cycle_data_id` = `OLD`.`life_cycle_data_id`;
        END;
    """

