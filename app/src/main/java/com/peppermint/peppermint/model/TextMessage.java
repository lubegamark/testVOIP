package com.peppermint.peppermint.model;

public class TextMessage {
String messageText;
Long time;
Enum status;
String sender;
Boolean isMine;
int dbId;

public TextMessage(String messageText, Long time, Boolean mine){
	this.messageText = messageText;
	this.time = time;
	this.isMine = mine;
}
public TextMessage(String messageText, Long time, Boolean mine, String sender){
	this.messageText = messageText;
	this.time = time;
	this.isMine = mine;
	this.sender = sender;
}
public TextMessage(String messageText, Long time, Boolean mine, String sender, int id){
	this.messageText = messageText;
	this.time = time;
	this.isMine = mine;
	this.sender = sender;
	this.dbId = id;
}

public String getMessageText() {
	return messageText;
}
public void setMessageText(String messageText) {
	this.messageText = messageText;
}
public Long getTime() {
	return time;
}
public void setTime(Long time) {
	this.time = time;
}
public Enum getStatus() {
	return status;
}
public void setStatus(Enum status) {
	this.status = status;
}
public String getSender() {
	return sender;
}
public void setSender(String sender) {
	this.sender = sender;
}
public Boolean isMine() {
	return isMine;
}
public void setIsMine(Boolean isMine) {
	this.isMine = isMine;
}

public Boolean getIsMine() {
	return isMine;
}
public int getDbId() {
	return dbId;
}
public void setDbId(int dbId) {
	this.dbId = dbId;
}
}
