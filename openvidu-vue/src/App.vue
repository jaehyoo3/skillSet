<template>
  <div id="app">
    <div>
      <input v-model="newSessionId" placeholder="Enter new session ID">
      <button @click="createSession">Create Session</button>
    </div>
    <div>
      <input v-model="sessionIdInput" placeholder="Enter session ID to join">
      <button @click="joinSession">Join Session</button>
    </div>
    <div v-if="session" id="video-container"></div>
  </div>
</template>

<script>
import {ref} from 'vue';
import axios from 'axios';
import {OpenVidu} from 'openvidu-browser';

export default {
  setup() {
    const session = ref(null);
    const newSessionId = ref('');
    const sessionIdInput = ref('');
    const token = ref('');
    const openVidu = new OpenVidu();

    const createSession = async () => {
      if (!newSessionId.value) {
        alert('Please enter a session ID');
        return;
      }
      try {
        const response = await axios.post('/api/openvidu/sessions', {customSessionId: newSessionId.value});
        alert('Session created with ID: ' + response.data);
      } catch (error) {
        console.error('Failed to create session', error);
      }
    };

    const joinSession = async () => {
      if (!sessionIdInput.value) {
        alert('Please enter a session ID');
        return;
      }
      try {
        // 토큰 생성
        const tokenResponse = await axios.post(`/api/openvidu/sessions/${sessionIdInput.value}/connections`);
        token.value = tokenResponse.data;

        // OpenVidu 세션 초기화
        session.value = openVidu.initSession();

        // 스트림 생성 이벤트 처리
        session.value.on('streamCreated', (event) => {
          const subscriber = session.value.subscribe(event.stream, 'video-container');
          document.getElementById('video-container').appendChild(subscriber.videos[0].video);
        });

        // 세션 연결
        await session.value.connect(token.value);

        // 퍼블리셔 초기화 및 퍼블리시
        const publisher = openVidu.initPublisher('video-container');
        await session.value.publish(publisher);
      } catch (error) {
        console.error('Failed to join session', error);
      }
    };

    return {
      session,
      newSessionId,
      sessionIdInput,
      createSession,
      joinSession
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
