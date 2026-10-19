/**
 * RoadResQ API Client
 * Centralized fetch wrapper with JWT bearer authorization, error handling, and JSON parsing
 */

const API = {
  // If frontend is accessed from a separate port (e.g. 3000, 5000, 5500),
  // automatically route API requests to the backend server on port 8080.
  // If served directly via Spring Boot on port 8080, relative URLs are used.
  baseUrl: (window.location.port && window.location.port !== '8080')
    ? `${window.location.protocol}//${window.location.hostname}:8080`
    : '',

  resolveUrl(path) {
    if (!path) return '';
    if (path.startsWith('http://') || path.startsWith('https://')) return path;
    return `${this.baseUrl}${path}`;
  },

  getToken() {
    return localStorage.getItem('roadresq_token');
  },

  setToken(token) {
    if (token) {
      localStorage.setItem('roadresq_token', token);
    } else {
      localStorage.removeItem('roadresq_token');
    }
  },

  async request(endpoint, options = {}) {
    const url = `${this.baseUrl}${endpoint}`;
    const headers = {
      'Content-Type': 'application/json',
      ...(options.headers || {})
    };

    const token = this.getToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const config = {
      ...options,
      headers
    };

    try {
      const response = await fetch(url, config);

      if (response.status === 401 && !endpoint.includes('/api/auth/login')) {
        // Token expired or invalid
        console.warn('Authentication expired or unauthorized');
      }

      const contentType = response.headers.get('content-type');
      let data = null;
      if (contentType && contentType.includes('application/json')) {
        data = await response.json();
      } else {
        data = await response.text();
      }

      if (!response.ok) {
        const error = new Error((data && data.error) || (data && data.message) || `HTTP error ${response.status}`);
        error.status = response.status;
        error.data = data;
        throw error;
      }

      return data;
    } catch (err) {
      console.error(`API Error on [${options.method || 'GET'} ${endpoint}]:`, err);
      throw err;
    }
  },

  get(endpoint) {
    return this.request(endpoint, { method: 'GET' });
  },

  post(endpoint, body) {
    return this.request(endpoint, {
      method: 'POST',
      body: JSON.stringify(body)
    });
  },

  patch(endpoint, body) {
    return this.request(endpoint, {
      method: 'PATCH',
      body: JSON.stringify(body)
    });
  },

  delete(endpoint) {
    return this.request(endpoint, { method: 'DELETE' });
  },

  async uploadFormData(endpoint, formData) {
    const url = `${this.baseUrl}${endpoint}`;
    const headers = {};
    const token = this.getToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(url, {
      method: 'POST',
      headers,
      body: formData
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.error || 'Upload failed');
    }
    return data;
  }
};
