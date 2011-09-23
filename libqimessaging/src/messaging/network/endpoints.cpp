/*
*  Author(s):
*  - Chris  Kilner <ckilner@aldebaran-robotics.com>
*  - Cedric Gestes <gestes@aldebaran-robotics.com>
*
*  Copyright (C) 2010 Aldebaran Robotics
*/

#include "src/messaging/network/endpoints.hpp"
#include "src/messaging/network/platform.hpp"
#include <vector>
#include <cstdio>

namespace qi {
  namespace detail {

    std::string negotiateEndpoint(const EndpointContext& clientContext,
                                  const EndpointContext& serverContext,
                                  const MachineContext& serverMachineContext)
    {
      char endpoint[38]; // 19 for address, 10 for port, 9 for protocol
      if(clientContext.machineID.compare(serverContext.machineID) == 0) {
        // same machine
        if(clientContext.contextID.compare(serverContext.contextID) == 0) {
          // same network context
          sprintf(endpoint, "inproc://127.0.0.1:%d", serverContext.port);
        } else if(serverMachineContext.platformID == PlatformWindows) {
          // windows does not support IPC
          sprintf(endpoint, "tcp://127.0.0.1:%d", serverContext.port);
        } else {
          // mac and linux both support ipc
          sprintf(endpoint, "ipc:///tmp/qi_127.0.0.1:%d", serverContext.port);
        }
      } else {
        // default to tcp on public ip
        sprintf(endpoint, "tcp://%s:%d", serverMachineContext.publicIP.c_str(), serverContext.port);
      }
      return endpoint;
    }

    std::vector<std::string> getEndpoints(const EndpointContext serverContext,
                                          const MachineContext& serverMachineContext)
    {
      std::vector<std::string> endpoints;
      char port[10]; // 10 for port
      sprintf(port, "%d", serverContext.port);

      endpoints.push_back(std::string("inproc://127.0.0.1:") + port);
      if (serverMachineContext.platformID != PlatformWindows) {
        // windows does not support IPC
        endpoints.push_back(std::string("ipc:///tmp/qi_127.0.0.1:") + port);
      }
      endpoints.push_back(std::string("tcp://127.0.0.1:") + port);
      if (!serverMachineContext.publicIP.empty()) {
        endpoints.push_back(std::string("tcp://") + serverMachineContext.publicIP + std::string(":") + port);
      }

      return endpoints;
    }
  }
}

