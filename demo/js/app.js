import React from 'react';
import axios from 'axios';
import lodash from 'lodash';
import { useState } from 'react';

function App() {

  const [user, setUser] = useState(null);

  const [loading, setLoading] = useState(false);

  const fetchUser = async (id) =>{
    setLoading(true);
    try {
      const res = await axios.get(`/api/users/${id}`);
      setUser(res.data);
    }catch(err) {
      console.error(err);
    }finally {
      setLoading(false);
    }
  };

  return (
    <div className="app">
      {}
      <h1>Hello World</h1>
      {loading && <p>Loading...</p>}
    </div>
  );
}

export default App;