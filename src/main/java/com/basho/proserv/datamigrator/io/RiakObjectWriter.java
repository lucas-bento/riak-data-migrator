package com.basho.proserv.datamigrator.io;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.basho.riak.pbc.RiakObject;
import com.basho.riak.pbc.RiakObjectIO;
import com.google.protobuf.ByteString;

public class RiakObjectWriter implements IRiakObjectWriter {
	private final Logger log = LoggerFactory.getLogger(RiakObjectWriter.class);
	
	private final DataOutputStream dataOutputStream;
	private final RiakObjectIO riakObjectIo = new RiakObjectIO();
	private final KeyJournal keyJournal;
	
	public RiakObjectWriter(File file) {
		try {
			this.dataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("File could not be created " + file.getAbsolutePath());
		}
		
		this.keyJournal = new KeyJournal(KeyJournal.createKeyPathFromPath(file), KeyJournal.Mode.WRITE);
	}
	
	public boolean writeRiakObject(RiakObject riakObject) {
		try {
			riakObjectIo.writeRiakObject(this.dataOutputStream, riakObject);
			keyJournal.write(riakObject);
		} catch (IOException ex) {
			log.error("Could not write RiakObject to outputStream", ex);
			this.close();
			return false;
		}
		
		return true;
	}
	
	public void close() {
		try {
			this.writeEOFRiakObject();
			this.dataOutputStream.flush();
			this.dataOutputStream.close();
		} catch (IOException e) {
			log.error("Could not close RiakObjectWriter file", e);
		}
	}
	
	private void writeEOFRiakObject() throws IOException {
		RiakObject riakObject = new RiakObject(ByteString.copyFromUtf8(""),
											   ByteString.copyFromUtf8(""),
											   ByteString.copyFromUtf8(""),
											   ByteString.copyFromUtf8(""));
		riakObjectIo.writeRiakObject(this.dataOutputStream, riakObject, 255);
	}
	
}
