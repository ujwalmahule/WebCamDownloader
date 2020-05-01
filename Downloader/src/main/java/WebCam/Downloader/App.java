package WebCam.Downloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class App {
	public static void main(String[] args) {
		final int MAX_FILES = 300;
		String sourceIP = "200.200.1.6:8080";
		String destination = "c:\\rec";
		String filePrefix = "rec";
		VideoFileNameComparator comparator = new VideoFileNameComparator();
		TreeSet<VideoFileName> files = new TreeSet<>(comparator);
		TreeSet<VideoFileName> destFiles = new TreeSet<>(comparator);
		
		
		getDestFiles(destFiles, destination);		
		getVideoFiles(files, sourceIP);
		
		System.out.println("Total files on CAM:" + files.size());
		
		//remove current file, as it might still be getting recorded
		if(files.size()>0) {
			files.remove(files.last());
		}
		
		int counter = 0;
		File downloadedFile;
		for(VideoFileName file : files) {
			try {
				downloadedFile = downloadFile(file, destination, filePrefix);
				if(downloadedFile != null) { 
					VideoFileName fileName = new VideoFileName(downloadedFile);
					destFiles.add(fileName);
					clean(destFiles, MAX_FILES);
				} 
			} catch(IOException e) {
				counter++; 
			}
			if(counter > 2) {
				break;
			}
		}
	}

	private static void clean(TreeSet<VideoFileName> destFiles, int maxFiles) {
		while(destFiles.size()>maxFiles) {
			VideoFileName videoFile = destFiles.first();
			File file = videoFile.getFile();
			if(file.exists()) {
				file.delete();
			}
			destFiles.remove(videoFile);
		}
		
	}

	private static void getDestFiles(TreeSet<VideoFileName> destFiles, String destination) {
		File dest = new File(destination);
		File[] files = dest.listFiles();
		for(File file : files) {
			if(file.isFile()) {
				destFiles.add(new VideoFileName(file));
			}
		}
		
	}

	private static void getVideoFiles(TreeSet<VideoFileName> files, String sourceIP) {
		JSONArray videos;
		try {
			videos = readJsonArrayFromUrl("http://" + sourceIP + "/list_videos");
			for(Object obj : videos) {
				VideoFileName fileName = new VideoFileName(sourceIP, (JSONObject) obj);
				files.add(fileName);
			}
		} catch (JSONException | IOException e) {
			System.err.println("Alert! cannot connect to " + sourceIP + " '" + e.getMessage() +"'");
		}
	}

	private static File downloadFile(VideoFileName videoFile, String destination, String prefix) throws IOException {
		String src = videoFile.getFileName();
		String dest = destination + File.separator + prefix + "_" + formatDate(videoFile.getTimeStamp()) + "." + videoFile.getExt();
		ReadableByteChannel rbc = null;
		FileOutputStream fos = null;
		
		File file = new File(dest);
		if(file.exists() && file.length() == videoFile.getSize()) {
			return null;
		}
		
		System.out.println("Downloading: " + src + " => " + dest);
		
        try {
        	URL url = new URL(src);
        	rbc = Channels.newChannel(url.openStream());
	        fos = new FileOutputStream(dest);
	        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch(IOException e) {
        	System.err.print("Download error:" + e.getMessage());
        } finally {
        	if(fos!=null) {
        		try {
					fos.close();
				} catch (IOException e) {
					//do nothing
				}
        	}
        	if(rbc!=null) {
        		try {
					rbc.close();
				} catch (IOException e) {
					//do nothing
				}
        	}
        }
        
        if(!file.exists() || file.length() != videoFile.getSize()) {
			System.out.println("Download failed");
			throw new IOException("Error downloading file");
		}
        
        return file;
	}

	private static String formatDate(Date timeStamp) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
		return format.format(timeStamp);
	}

	public static JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONArray json = new JSONArray(jsonText);
			return json;
		} finally {
			is.close();
		}
	}

	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}
}
