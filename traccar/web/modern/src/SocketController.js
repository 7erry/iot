import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { connect } from 'react-redux';
import { positionsActions, devicesActions, sessionActions } from './store';
import { useHistory } from 'react-router-dom';

const displayNotifications = events => {
  if ("Notification" in window) {
    if (Notification.permission === "granted") {
      for (const event of events) {
        const notification = new Notification(`Event: ${event.type}`);
        setTimeout(notification.close.bind(notification), 4 * 1000);
      }
    } else if (Notification.permission !== "denied") {
      Notification.requestPermission(permission => {
        if (permission === "granted") {
          displayNotifications(events);
        }
      });
    }
  }
};

const SocketController = () => {
  const dispatch = useDispatch();
  const history = useHistory();
  const authenticated = useSelector(state => state.session.authenticated);

  const connectSocket = () => {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const socket = new WebSocket(protocol + '//' + window.location.host + '/api/socket');

    socket.onclose = () => {
      setTimeout(() => connectSocket(), 60 * 1000);
    };

    socket.onmessage = (event) => {
      const data = JSON.parse(event.data);
      if (data.devices) {
        dispatch(devicesActions.update(data.devices));
      }
      if (data.positions) {
        dispatch(positionsActions.update(data.positions));
      }
      if (data.events) {
        displayNotifications(data.events);
      }
    };
  }

  useEffect(() => {
    if (authenticated) {
      fetch('/api/devices').then(response => {
        if (response.ok) {
          response.json().then(devices => {
            dispatch(devicesActions.update(devices));
          });
        }
        connectSocket();
      });
    } else {
      fetch('/api/session').then(response => {
        if (response.ok) {
          dispatch(sessionActions.authenticated(true));
        } else {
          history.push('/login');
        }
      });
    }
  }, [authenticated]);

  return null;
}

export default connect()(SocketController);
