<template>
  <div id="app">
    <div>
      <input v-model="newSessionId" placeholder="Enter new session ID">
      <button @click="createSession">Create Session</button>
    </div>
    <div>
      <h2>Active Sessions</h2>
      <ul>
        <li v-for="session in sessions" :key="session">
          {{ session }}
          <button @click="joinExistingSession(session)">Join</button>
        </li>
      </ul>
      <button @click="fetchSessions">Refresh Sessions</button>
    </div>
    <div>
      <input v-model="sessionIdInput" placeholder="Enter session ID to join">
      <button @click="joinSession">Join Session</button>
    </div>
    <div v-if="session" id="video-container"></div>
    <div v-if="session">
      <input v-model="chatMessage" placeholder="Enter your message">
      <button @click="sendMessage">Send Message</button>
    </div>
    <div v-if="session">
      <h2>Chat Messages</h2>
      <ul>
        <li v-for="(msg, index) in chatMessages" :key="index">
          {{ msg.sender }}: {{ msg.message }}
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { OpenVidu } from 'openvidu-browser';

export default {
  setup() {
    const session = ref(null);
    const newSessionId = ref('');
    const sessionIdInput = ref('');
    const token = ref('');
    const sessions = ref([]);
    const chatMessage = ref('');
    const chatMessages = ref([]);
    const openVidu = new OpenVidu();

    const createSession = async () => {
      if (!newSessionId.value) {
        alert('Please enter a session ID');
        return;
      }
      try {
        const response = await axios.post('/api/openvidu/sessions', { customSessionId: newSessionId.value });
        alert('Session created with ID: ' + response.data);
        fetchSessions(); // Refresh the list of sessions
      } catch (error) {
        console.error('Failed to create session', error);
      }
    };

    const fetchSessions = async () => {
      try {
        const response = await axios.get('/api/openvidu/sessions');
        sessions.value = response.data;
      } catch (error) {
        console.error('Failed to fetch sessions', error);
      }
    };

    const joinExistingSession = async (sessionId) => {
      sessionIdInput.value = sessionId;
      await joinSession();
    };

    const joinSession = async () => {
      if (!sessionIdInput.value) {
        alert('Please enter a session ID');
        return;
      }
      try {
        // Generate token
        const tokenResponse = await axios.post(`/api/openvidu/sessions/${sessionIdInput.value}/connections`);
        token.value = tokenResponse.data;

        // Initialize OpenVidu session
        session.value = openVidu.initSession();

        // Handle stream created event
        session.value.on('streamCreated', (event) => {
          const subscriber = session.value.subscribe(event.stream, 'video-container');
          document.getElementById('video-container').appendChild(subscriber.videos[0].video);
        });

        // Handle signal event for chat messages
        session.value.on('signal:chat', (event) => {
          const message = JSON.parse(event.data);
          chatMessages.value.push({ sender: message.sender, message: message.message });
        });

        // Connect to the session
        await session.value.connect(token.value);

        // Initialize and publish the stream
        const publisher = openVidu.initPublisher('video-container');
        await session.value.publish(publisher);
      } catch (error) {
        console.error('Failed to join session', error);
      }
    };

    const sendMessage = async () => {
      if (!chatMessage.value) {
        alert('Please enter a message');
        return;
      }
      try {
        const message = { sender: 'Me', message: chatMessage.value };
        session.value.signal({
          type: 'chat',
          data: JSON.stringify(message)
        });
        chatMessages.value.push(message);
        chatMessage.value = ''; // Clear the input after sending the message
      } catch (error) {
        console.error('Failed to send message', error);
      }
    };

    onMounted(() => {
      fetchSessions();
    });

    return {
      session,
      newSessionId,
      sessionIdInput,
      token,
      sessions,
      chatMessage,
      chatMessages,
      createSession,
      joinExistingSession,
      joinSession,
      fetchSessions,
      sendMessage
    };
  }
};
</script>

<style>
#video-container {
  display: flex;
  flex-wrap: wrap;
}

#video-container video {
  width: 50%;
}
</style>
