/*
 * Copyright 2014 William Seemann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wseemann.media.jplaylistparser.parser.m3u;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Set;

import org.xml.sax.SAXException;

import wseemann.media.jplaylistparser.exception.JPlaylistParserException;
import wseemann.media.jplaylistparser.mime.MediaType;
import wseemann.media.jplaylistparser.parser.AbstractParser;
import wseemann.media.jplaylistparser.playlist.Playlist;
import wseemann.media.jplaylistparser.playlist.PlaylistEntry;

public class M3UPlaylistParser extends AbstractParser {
	public final static String EXTENSION = ".m3u";
	
    private final static String EXTENDED_INFO_TAG = "#EXTM3U";
    private final static String RECORD_TAG = "^[#][E|e][X|x][T|t][I|i][N|n][F|f].*";
	
	private static int mNumberOfFiles = 0;
    private boolean processingEntry = false;
    
    private static final Set<MediaType> SUPPORTED_TYPES =
    		Collections.singleton(MediaType.audio("x-mpegurl"));

    public Set<MediaType> getSupportedTypes() {
    	return SUPPORTED_TYPES;
    }
    
	/**
	 * Retrieves the files listed in a .m3u file
	 * @throws IOException 
	 */
    private void parsePlaylist(InputStream stream, Playlist playlist) throws IOException {
        String line = null;
        BufferedReader reader = null;
        PlaylistEntry playlistEntry = null;
        
		// Start the query
		reader = new BufferedReader(new InputStreamReader(stream));
        
		while ((line = reader.readLine()) != null) {
			if (!(line.equalsIgnoreCase(EXTENDED_INFO_TAG) || line.trim().equals(""))) {
		    	if (line.matches(RECORD_TAG)) {
		    		playlistEntry = new PlaylistEntry();
		        	playlistEntry.set(PlaylistEntry.PLAYLIST_METADATA, line.replaceAll("^(.*?),", ""));
		    		processingEntry = true;
		    	} else {
		    		if (!processingEntry) {
		    			playlistEntry = new PlaylistEntry();
		    		}
		    		
		    		playlistEntry.set(PlaylistEntry.URI, line.trim());
		    		savePlaylistFile(playlistEntry, playlist);
		    	}
		    }           
        }
    }

    private void savePlaylistFile(PlaylistEntry playlistEntry, Playlist playlist) {
    	mNumberOfFiles = mNumberOfFiles + 1;
    	playlistEntry.set(PlaylistEntry.TRACK, String.valueOf(mNumberOfFiles));
    	parseEntry(playlistEntry, playlist);
    	processingEntry = false;
    }

	@Override
	public void parse(String uri, InputStream stream, Playlist playlist)
			throws IOException, SAXException, JPlaylistParserException {
		parsePlaylist(stream, playlist);
	}
}

