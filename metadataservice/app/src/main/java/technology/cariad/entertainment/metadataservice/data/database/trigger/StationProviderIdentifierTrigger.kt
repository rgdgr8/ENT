package technology.cariad.entertainment.metadataservice.data.database.trigger

//language=SQL
const val TRIGGER_STATION_PROVIDER_IDENTIFIER_AFTER_DELETE =
    """
        CREATE TRIGGER `trigger_station_provider_identifier_after_delete`
        AFTER DELETE ON `station_provider_identifier`
        WHEN NOT EXISTS (
            SELECT 1 FROM `station_provider_identifier` WHERE `provider_id` = `OLD`.`provider_id`
        )
        BEGIN
            DELETE FROM `provider_identifier` WHERE `provider_id` = `OLD`.`provider_id`;
        END;
    """

