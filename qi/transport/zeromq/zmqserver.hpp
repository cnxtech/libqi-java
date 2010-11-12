/*
** Author(s):
**  - Cedric GESTES      <gestes@aldebaran-robotics.com>
**
** Copyright (C) 2010 Aldebaran Robotics
*/

#ifndef QI_TRANSPORT_ZEROMQSERVER_HPP_
#define QI_TRANSPORT_ZEROMQSERVER_HPP_

#include <zmq.hpp>
#include <qi/transport/server.hpp>
#include <qi/transport/common/handlers_pool.hpp>
#include <qi/transport/zeromq/zmqserver.hpp>
#include <string>
#include <boost/thread/mutex.hpp>

namespace qi {
  namespace transport {

    /// <summary>
    /// The server class. It listen for incoming connection from client
    /// and push handlers for those connection to the tread pool.
    /// This class need to be instantiated and run at the beginning of the process.
    /// </summary>
    class ResultHandler;
    class ZMQServer : public Server, public Detail::IServerResponseHandler {
    public:
      /// <summary> The Server class constructor. </summary>
      /// <param name="server_name">
      /// The name given to the server, id for clients to connect.
      /// </param>
      ZMQServer(const std::string & server_name);

      /// <summary> The Server class destructor </summary>
      virtual ~ZMQServer();

      /// <summary> Run the server thread. </summary>
      virtual void run();

      /// <summary> Wait for the server thread to complete its task. </summary>
      void wait();

      zmq::message_t *recv(zmq::message_t &msg);

      /// <summary> Force the server to stop and wait for complete stop. </summary>
      void stop();

      void poll();
      void serverResponseHandler(const std::string &result, void *data = 0);

      ResultHandler *getResultHandler() { return 0; }

      friend void *worker_routine(void *arg);

    private:
      bool                server_running;
      zmq::context_t      zctx;
      zmq::socket_t       zsocket;
      boost::mutex        socketMutex;
      HandlersPool        handlersPool;
    };
  }
}

#endif  // QI_TRANSPORT_ZEROMQSERVER_HPP_
