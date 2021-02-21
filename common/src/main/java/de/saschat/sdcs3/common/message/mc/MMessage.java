package de.saschat.sdcs3.common.message.mc;
/*
    Message on Discord: Author, Picture, Content
    Message on Minecraft:
        Chat:   Author (-> der. Picture), Content
        System: Recipient
 */
// @TODO: Language
public abstract class MMessage {
    public Person User;
    public static class Person {
        public String UUID;
        public String Username;
    }
}
