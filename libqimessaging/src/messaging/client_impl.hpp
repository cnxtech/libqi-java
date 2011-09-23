#pragma once
/*
*  Author(s):
*  - Chris  Kilner <ckilner@aldebaran-robotics.com>
*  - Cedric Gestes <gestes@aldebaran-robotics.com>
*
*  Copyright (C) 2010 Aldebaran Robotics
*/


#ifndef _QI_MESSAGING_SRC_CLIENT_IMPL_HPP_
#define _QI_MESSAGING_SRC_CLIENT_IMPL_HPP_

#include <string>
#include <boost/shared_ptr.hpp>
#include "src/messaging/mutexednamelookup.hpp"
#include "src/messaging/impl_base.hpp"
#include <qi/serialization/message.hpp>


namespace qi {
  namespace detail {

    class ClientImpl : public ImplBase {
    public:
      ClientImpl(const std::string &clientName = "", Context *ctx = 0);
      virtual ~ClientImpl();
      void connect(const std::string &masterAddress = "127.0.0.1:5555");

      void call(const std::string &signature,
                const qi::serialization::Message& callDef,
                qi::serialization::Message &result);

      std::string endpointId() { return _endpointContext.endpointID; }

    private:
      MutexedNameLookup<std::string> _serviceCache;

      // map from address to Client
      MutexedNameLookup< boost::shared_ptr<qi::transport::TransportClient> > _serverClients;

      boost::shared_ptr<qi::transport::TransportClient> xGetServerClient(
        const std::string& serverAddress);

      bool xCreateServerClient(const std::string& address);
      const std::string& xLocateService(const std::string& methodHash);
    };

  }
}

#endif  // _QI_MESSAGING_SRC_CLIENT_IMPL_HPP_