import React, { useState, useEffect } from 'react';
import axios from 'axios';

function App() {
  // State for authenticated user
  const [user, setUser] = useState(null);
  // State for files fetched from Google Drive
  const [files, setFiles] = useState([]);
  // State for the file selected for upload
  const [selectedFile, setSelectedFile] = useState(null);
  // State for error messages
  const [error, setError] = useState('');
  // Loading state for file list
  const [loading, setLoading] = useState(false);

  // Fetch user details from backend if authenticated
  const fetchUser = async () => {
    try {
      const response = await axios.get('http://localhost:8080/user', { withCredentials: true });
      setUser(response.data);
    } catch (err) {
      console.error('Error fetching user:', err);
    }
  };

  // Fetch list of files from Google Drive
  const fetchFiles = async () => {
    try {
      setLoading(true);
      const response = await axios.get('http://localhost:8080/drive/files', { withCredentials: true });
      // If response is a JSON string, parse it
      const data = typeof response.data === 'string' ? JSON.parse(response.data) : response.data;
      setFiles(data.files || []);
      setError('');
    } catch (err) {
      console.error('Error fetching files:', err);
      setError('Error fetching files. Are you authenticated?');
    } finally {
      setLoading(false);
    }
  };

  // Upload file handler
  const handleFileUpload = async (e) => {
    e.preventDefault();
    if (!selectedFile) return;
    const formData = new FormData();
    formData.append('file', selectedFile);
    try {
      await axios.post('http://localhost:8080/drive/upload', formData, {
        withCredentials: true,
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      setError('');
      setSelectedFile(null);
      // Refresh file list after successful upload
      fetchFiles();
    } catch (err) {
      console.error('Error uploading file:', err);
      setError('Error uploading file');
    }
  };

  // Delete file handler
  const handleDelete = async (fileId) => {
    try {
      await axios.delete(`http://localhost:8080/drive/files/${fileId}`, { withCredentials: true });
      setError('');
      // Refresh file list after deletion
      fetchFiles();
    } catch (err) {
      console.error('Error deleting file:', err);
      setError('Error deleting file');
    }
  };


  
  const handleDownload = async (fileId) => {
    try {
      const response = await axios.get(`http://localhost:8080/drive/files/download/${fileId}`, { 
        withCredentials: true,
        responseType: 'blob' // Ensure binary data is handled properly.
      });
      
      // Create a blob from the response data.
      const blob = new Blob([response.data], { type: response.headers['content-type'] });
      
      // Optionally, try to extract the filename from the content-disposition header.
      let filename = 'downloadedFile';
      const disposition = response.headers['content-disposition'];
      if (disposition && disposition.indexOf('filename=') !== -1) {
        const filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
        const matches = filenameRegex.exec(disposition);
        if (matches != null && matches[1]) { 
            filename = matches[1].replace(/['"]/g, '');
        }
      }
      
      // Create a temporary download link and trigger it.
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', filename);
      document.body.appendChild(link);
      link.click();
      
      // Clean up the link.
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      setError('');
      
      // Refresh file list if needed.
      fetchFiles();
    } catch (err) {
      console.error('Error downloading file:', err);
      setError('Error downloading file');
    }
  };
  

  // On component mount, fetch user info
  useEffect(() => {
    fetchUser();
  }, []);

  // When user is authenticated, fetch files
  useEffect(() => {
    if (user) {
      fetchFiles();
    }
  }, [user]);

  return (
    <div style={{ margin: '20px' }}>
      <h1>DriveSync App</h1>
      {user ? (
        <>
          <p>Welcome, {user.name || user.email || 'User'}!</p>
          <button onClick={fetchFiles}>Refresh Files</button>
          {error && <p style={{ color: 'red' }}>{error}</p>}
          <h2>Your Google Drive Files</h2>
          {loading ? (
            <p>Loading files...</p>
          ) : files.length ? (
            <ul>
              {files.map((file) => (
                <li key={file.id}>
                  {file.name}{' '}
                  <button onClick={() => handleDelete(file.id)}>Delete</button>
                  <button onClick={() => handleDownload(file.id)}>Download</button>
                </li>
              ))}
            </ul>
          ) : (
            <p>No files found.</p>
          )}
          <h2>Upload a File</h2>
          <form onSubmit={handleFileUpload}>
            <input type="file" onChange={(e) => setSelectedFile(e.target.files[0])} />
            <button type="submit">Upload</button>
          </form>
        </>
      ) : (
        <div>
          <p>You are not logged in.</p>
          {/* Redirect to the OAuth2 endpoint to start login */}
          <a href="http://localhost:8080/oauth2/authorization/google">
            <button>Login with Google</button>
          </a>
        </div>
      )}
    </div>
  );
}

export default App;
