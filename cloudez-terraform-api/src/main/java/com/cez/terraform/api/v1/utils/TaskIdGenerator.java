package com.cez.terraform.api.v1.utils;

import java.util.Random;

public class TaskIdGenerator
{

  public static String getId()
  {
    int leftLimit = 97; // letter 'a'
    int rightLimit = 122; // letter 'z'
    int targetStringLength = 16;
    Random random = new Random();
    StringBuilder buffer = new StringBuilder(targetStringLength);
    for (int i = 0; i < targetStringLength; i++)
    {
      int randomLimitedInt = leftLimit + (int) (random.nextFloat() * (rightLimit - leftLimit + 1));
      buffer.append((char) randomLimitedInt);
    }
    String generatedString = buffer.toString();
    return generatedString.toUpperCase();
  }
}
