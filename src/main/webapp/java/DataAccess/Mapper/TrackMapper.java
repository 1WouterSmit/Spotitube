package DataAccess.Mapper;

import DataAccess.DB;
import Domain.Track;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class TrackMapper {

	protected String findAllInPLStatement() {
		return "SELECT * " +
				"FROM playlist_tracks " +
				"LEFT JOIN tracks " +
				"ON playlist_tracks.track_id = tracks.id " +
				"HAVING playlist_tracks.playlist_id = ?";
	}

	protected String findAllNotInPLStatement() {
		return "SELECT * " +
				"FROM tracks " +
				"WHERE id NOT IN " +
				"(SELECT track_id " +
				"FROM spot.playlist_tracks " +
				"WHERE playlist_id = ?)";
	}

	protected Track load(ResultSet rs) throws SQLException {
		Track track = new Track();
		track.setId(rs.getLong("id"));
		track.setTitle(rs.getString("title"));
		track.setPerformer(rs.getString("performer"));
		track.setDuration(rs.getInt("duration"));
		track.setAlbum(rs.getString("album"));

		Long playcount = rs.getLong("playcount");
		if(rs.wasNull()) {
			playcount = null;
		}
		track.setPlaycount(playcount);

		track.setPublicationDate(rs.getString("publication_date"));
		track.setDescription(rs.getString("description"));
		return track;
	}

	public ArrayList<Track> findAllInPlaylist(Long playlistId) throws SQLException {
		PreparedStatement getAllInPlaylist = DB.connection().prepareStatement(findAllInPLStatement());
		getAllInPlaylist.setLong(1, playlistId);
		ResultSet rs = getAllInPlaylist.executeQuery();

		return getTrackListFromSet(rs);
	}

	public ArrayList<Track> findAllNotInPlaylist(Long playlistId) throws SQLException {
		PreparedStatement getAllInPlaylist = DB.connection().prepareStatement(findAllNotInPLStatement());
		getAllInPlaylist.setLong(1, playlistId);
		ResultSet rs = getAllInPlaylist.executeQuery();

		return getTrackListFromSet(rs);
	}

	private ArrayList<Track> getTrackListFromSet(ResultSet rs) throws SQLException {
		ArrayList<Track> tracks = new ArrayList<>();
		while(rs.next()) {
			Track track = load(rs);
			tracks.add(track);
		}
		return tracks;
	}

	protected String addToPlaylistStatement() {
		return "INSERT INTO playlist_tracks (playlist_id, track_id)" +
				" VALUES (?, ?)";
	}

	protected String removeTrackFromPlaylistStatement() {
		return "DELETE FROM playlist_tracks " +
				"WHERE playlist_id = ? " +
				"AND track_id = ?";
	}

	public void addTrackToPlaylist(Long playlistId, Track track) throws SQLException {
		PreparedStatement statement = DB.connection().prepareStatement(addToPlaylistStatement());
		statement.setLong(1, playlistId);
		statement.setLong(2, track.getId());
		statement.execute();
	}

	public void removeTrackFromPlaylist(Long playlistId, Long trackId) throws SQLException {
		PreparedStatement statement = DB.connection().prepareStatement(removeTrackFromPlaylistStatement());
		statement.setLong(1, playlistId);
		statement.setLong(2, trackId);
		statement.execute();
	}
}
