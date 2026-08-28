package technology.cariad.entertainment.metadataservice.data.database.trigger

//language=SQL
const val TRIGGER_STATION_GENRE_RANK_AFTER_DELETE =
    """
        CREATE TRIGGER `trigger_station_genre_rank_after_delete`
        AFTER DELETE ON `station_genre_rank`
        WHEN NOT EXISTS (SELECT 1 FROM `station_genre_rank` WHERE `genre_id` = `OLD`.`genre_id`)
        BEGIN
            DELETE FROM `genre` WHERE `genre_id` = `OLD`.`genre_id`;
        END;
    """

