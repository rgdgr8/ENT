package technology.cariad.entertainment.metadataservice.data.database.trigger

//language=SQL
const val TRIGGER_STATION_AFTER_DELETE =
    """
        CREATE TRIGGER `trigger_station_after_delete`
        AFTER DELETE ON `station` 
        BEGIN
            DELETE FROM `life_cycle_data` WHERE `life_cycle_data_id` = `OLD`.`life_cycle_data_id`;
        END;
    """

