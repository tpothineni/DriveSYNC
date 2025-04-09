import React, { useState, useEffect } from 'react';
import axios from 'axios';

//Extract CSRF token from cookie set by spring
function getCsrfTokenFromCookie() {
  const match = document.cookie.match(/XSRF-TOKEN=([^;]+)/);
  return match ? decodeURIComponent(match[1]) : null;
}

//setting the url
axios.defaults.baseURL = 'http://localhost:8080';
axios.defaults.withCredentials = true;

//Adds X-XSRF-TOKEN for all outgoing requests
axios.interceptors.request.use((config) => {
  const token = getCsrfTokenFromCookie();
  if (token) {
    config.headers['X-XSRF-TOKEN'] = token;
  }
  return config;
});

//Refreshs the CSRF Token if needed
async function refreshCsrfToken() {
  try {
    const res = await axios.get('/csrf-token');
    const token = res.data.token;
    document.cookie = `XSRF-TOKEN=${token}; path=/`;
  } catch (err) {
    console.error('Could not refresh CSRF token:', err);
  }
}

//manages states of user, files, upload
function App() {
  const [user, setUser] = useState(null);
  const [files, setFiles] = useState([]);
  const [selectedFile, setSelectedFile] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  //intially, fetch's user and files if authenticated
  useEffect(() => {
    const fetchUser = async () => {
      try {
        const response = await axios.get('/user');
        setUser(response.data);
        await refreshCsrfToken();
        fetchFiles();
      } catch (err) {
        if (err.response?.status === 401) {
          setUser(null);
        } else {
          console.error('Error fetching user:', err);
          setError('Error fetching user');
        }
      }
    };

    fetchUser();
  }, []);

  //fetches the files and updates the UI
  const fetchFiles = async () => {
    try {
      setLoading(true);
      const response = await axios.get('/drive/files');
      const data = typeof response.data === 'string' ? JSON.parse(response.data) : response.data;
      setFiles(data.files || []);
    } catch (err) {
      console.error('Error fetching files:', err);
      setError('Error fetching files');
    } finally {
      setLoading(false);
    }
  };

  //Submits a file using multipart/form-data
  const handleFileUpload = async (e) => {
    e.preventDefault();
    if (!selectedFile) return;

    const formData = new FormData();
    formData.append('file', selectedFile);

    try {
      await axios.post('/drive/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      setSelectedFile(null);
      await refreshCsrfToken();
      fetchFiles();
    } catch (err) {
      console.error('Error uploading file:', err);
      setError('Error uploading file');
    }
  };

  //sending request for deleting and updating the UI
  const handleDelete = async (fileId) => {
    try {
      await refreshCsrfToken(); 
      await axios.post(`/drive/delete/${fileId}`, {});
      fetchFiles();
    } catch (err) {
      console.error('Error deleting file:', err);
      setError('Error deleting file');
    }
  };

  //Downloading the file
  //Converts the blob file to downloadable file and triggers the browser download action
  const handleDownload = async (fileId) => {
    try {
      const response = await axios.get(`/drive/download/${fileId}`, {
        responseType: 'blob',
      });

      const blob = new Blob([response.data], {
        type: response.headers['content-type'],
      });

      let filename = 'downloadedFile';
      const disposition = response.headers['content-disposition'];
      if (disposition && disposition.includes('filename=')) {
        const match = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/.exec(disposition);
        if (match && match[1]) {
          filename = match[1].replace(/['"]/g, '');
        }
      }

      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', filename);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (err) {
      console.error('Error downloading file:', err);
      setError('Error downloading file');
    }
  };

  //Logout the user and redirect to homepage
  const handleLogout = async () => {
    try {
      await refreshCsrfToken(); 
      await axios.post('/logout', {});
      setUser(null);
      setFiles([]);
      setSelectedFile(null);
      setError('');
      window.location.href = 'http://localhost:3000';
    } catch (err) {
      console.error('Logout failed:', err);
    }
  };

  return (
    <div style={{ margin: '20px' }}>
      <h1>DriveSync App</h1>

      {user ? (
        <>
          <p>Welcome, {user.name || user.email || 'User'}!</p>

          <button onClick={handleLogout}>Logout</button>
          <button onClick={fetchFiles} style={{ marginLeft: '10px' }}>Refresh Files</button>

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
            <input
              type="file"
              onChange={(e) => setSelectedFile(e.target.files[0])}
            />
            <button type="submit">Upload</button>
          </form>
        </>
      ) : (
        <div>
          <p>You are not logged in.</p>
          <button
            onClick={() =>
              window.location.href = 'http://localhost:8080/oauth2/authorization/google'
            }
          >
            Login with Google
          </button>
        </div>
      )}
    </div>
  );
}

export default App;