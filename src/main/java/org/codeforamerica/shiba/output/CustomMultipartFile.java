package org.codeforamerica.shiba.output;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

public class CustomMultipartFile implements MultipartFile {
	
	

	public CustomMultipartFile(byte[] input, String name, String originalFilename, String contentType) {
		super();
		this.input = input;
		this.name = name;
		this.originalFilename = originalFilename;
		this.contentType = contentType;
	}

	private byte[] input;
	private String name;
	private String originalFilename;
	private String contentType;
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getOriginalFilename() {
		return originalFilename;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public boolean isEmpty() {
		 return input == null || input.length == 0;
	}

	@Override
	public long getSize() {
		 return input.length;
	}

	@Override
	public byte[] getBytes() throws IOException {
		return input;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		 return new ByteArrayInputStream(input);
	}

	@Override
	public void transferTo(File dest) throws IOException, IllegalStateException {
		try(FileOutputStream fos = new FileOutputStream(dest)) {
            fos.write(input);
        }
		
	}

}
