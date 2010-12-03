/*
*  Author(s):
*  - Chris  Kilner <ckilner@aldebaran-robotics.com>
*  - Cedric Gestes <gestes@aldebaran-robotics.com>
*
*  Copyright (C) 2010 Aldebaran Robotics
*/

#include <qi/transport/detail/zmq/zmq_client_impl.hpp>
#include <qi/exceptions/exceptions.hpp>
#include <iostream>

namespace qi {
  namespace transport {
    namespace detail {
      /// <summary> Constructor. </summary>
      /// <param name="serverAddress"> The server address. </param>
      ZMQClientImpl::ZMQClientImpl(const std::string &serverAddress)
        : ClientImpl<qi::transport::Buffer>(serverAddress),
        context(1),
        socket(context, ZMQ_REQ)
      {
        connect();
      }

      /// <summary> Connects to the server </summary>
      void ZMQClientImpl::connect()
      {
        socket.connect(_serverAddress.c_str());
        //TODO: check that the connection is OK
        //sleep(1);
      }

      void ZMQClientImpl::pollRecv(long timeout) {
        int             rc = 0;
        zmq_pollitem_t  items[1];

        items[0].socket  = socket;
        items[0].fd      = 0;
        items[0].events  = ZMQ_POLLIN;
        items[0].revents = 0;

        rc = zmq_poll(items, 1, timeout);
        std::cout << "timeout:" << timeout << std::endl;
        std::cout << "Rc:" << rc << std::endl;
        std::cout << "PollIn:" << (items[0].revents) << std::endl;
        if ((rc <= 0) || (!(items[0].revents & ZMQ_POLLIN)))
          throw qi::transport::Exception("no response");
      }

      /// <summary> Sends. </summary>
      /// <param name="tosend"> The data to send. </param>
      /// <param name="result"> [in,out] The result. </param>
      void ZMQClientImpl::send(const std::string &tosend, std::string &result)
      {
        // TODO optimise this
        // Could we copy from the serialized stream without calling
        // stream.str() before sending to this method?
        //TODO: could we avoid more copy?
        zmq::message_t msg(tosend.size());
        memcpy(msg.data(), tosend.data(), tosend.size());
        socket.send(msg);

        //we leave the possibility to timeout, pollRecv will throw and avoid the call to recv
        //pollRecv(1000 * 1000 * 1000);
        socket.recv(&msg);

        // TODO optimize this
        // boost could serialize from msg.data() and size,
        // without making a string
        result.assign((char *)msg.data(), msg.size());
      }
    }
  }
}

