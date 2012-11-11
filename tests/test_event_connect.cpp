/*
**
** Copyright (C) 2012 Aldebaran Robotics
*/


#include <map>
#include <gtest/gtest.h>
#include <qitype/genericobject.hpp>
#include <qitype/genericobjectbuilder.hpp>
#include <qi/application.hpp>

static int lastPayload = 0;
static int lastPayload2 = 0;
static int completed = 0;
void onFire(const int& pl)
{
  lastPayload = pl;
}
void onFire2(const int& pl)
{
  lastPayload2 = pl;
}


void testDelete(bool afirst, bool disconnectFirst)
{
  qi::GenericObjectBuilder oba, obb;
  unsigned int fireId = oba.advertiseEvent<void (*)(int)>("fire");
  unsigned int onFireId = obb.advertiseMethod("onFire", &onFire);
  unsigned int onFireId2 = obb.advertiseMethod("onFire2", &onFire2);
  qi::ObjectPtr *a = new qi::ObjectPtr(oba.object());
  qi::ObjectPtr *b = new qi::ObjectPtr(obb.object());
  unsigned int linkId = (*a)->connect(fireId, *b, onFireId);
  (*a)->connect(fireId, *b, onFireId2);
  //std::vector<qi::SignalSubscriber> subs = (*a)->subscribers(fireId);
  //EXPECT_EQ(static_cast<unsigned int>(2), subs.size());
  // Subs ordering is unspecified
  //EXPECT_EQ(subs[0].method + subs[1].method, onFireId + onFireId2);
  (*a)->emitEvent("fire", 12);
  EXPECT_EQ(12, lastPayload);
  EXPECT_EQ(12, lastPayload2);
  if (disconnectFirst)
  {
    (*a)->disconnect(linkId);
    (*a)->emitEvent("fire", 13);
    EXPECT_EQ(12, lastPayload);
    EXPECT_EQ(13, lastPayload2);
  }
  if (afirst)
  {
    delete a;
    delete b;
  }
  else
  {
    delete b;
    (*a)->emitEvent("fire", 12);
    delete a;
  }
  ++completed;
}

TEST(TestObject, Destruction)
{
  // Run test from object thread as they are synchronous
  for (int i=0; i<4; ++i)
    qi::getDefaultObjectEventLoop()->asyncCall(0,
      boost::bind(&testDelete, (bool)i/2, (bool)i%2));
  while (completed < 4)
    qi::os::msleep(100);
  /*
  testDelete(false, false);
  testDelete(true, true);
  testDelete(false, true);
  testDelete(true, false);
  */
}

int main(int argc, char **argv) {
  qi::Application app(argc, argv);
  ::testing::InitGoogleTest(&argc, argv);
  return RUN_ALL_TESTS();
}