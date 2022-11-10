package com.cez.terraform.api.v1.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class TimeUtil
{
  // private static String getCurrentUTCTime()
  // {
  // return Instant.now().toString();
  // }

  private static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

  public static Date getCurrentUTCDateWithTimeZone()
  {
    String date = ZonedDateTime.now(ZoneId.of("UTC")).toString();
    SimpleDateFormat inputFormat = new SimpleDateFormat(DATE_FORMAT);
    Date formattedDate = null;
    try
    {
      formattedDate = inputFormat.parse(date);
    }
    catch (ParseException e)
    {
      e.printStackTrace();
    }
    return formattedDate;
  }

  // public static Date getCurrentPSTDateWithTimeZone()
  // {
  // String date = ZonedDateTime.now(ZoneId.of("PST")).toString();
  // SimpleDateFormat inputFormat = new SimpleDateFormat(DATE_FORMAT);
  // Date formattedDate = null;
  // try
  // {
  // formattedDate = inputFormat.parse(date);
  // }
  // catch (ParseException e)
  // {
  // e.printStackTrace();
  // }
  // return formattedDate;
  // }

  public static String getCurrentUTCDate()
  {
    String date = ZonedDateTime.now(ZoneId.of("UTC")).toString();
    String formattedDate = null;
    SimpleDateFormat inputFormat = new SimpleDateFormat(DATE_FORMAT);
    SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
    Date parsedDate;
    try
    {
      parsedDate = inputFormat.parse(date);
      formattedDate = outputFormat.format(parsedDate);
    }
    catch (ParseException e)
    {
      e.printStackTrace();
    }

    return formattedDate;
  }

  public static String getCurrentUTCDateTime()
  {
    String date = ZonedDateTime.now(ZoneId.of("UTC")).toString();
    String formattedDate = null;
    SimpleDateFormat inputFormat = new SimpleDateFormat(DATE_FORMAT);
    SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd-h:m:s Z");
    Date parsedDate;
    try
    {
      parsedDate = inputFormat.parse(date);
      formattedDate = outputFormat.format(parsedDate);
    }
    catch (ParseException e)
    {
      e.printStackTrace();
    }

    return formattedDate;
  }

  public static String convertUTCDateTime(Date date)
  {
    SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd h:m:s Z");
    return outputFormat.format(date);
  }


  public static String getCurrentPSTDate()
  {
    return ZonedDateTime.now(ZoneId.of("PST")).toString();
  }

  public static String getUTCDateNextMonth()
  {
    String date = ZonedDateTime.now(ZoneId.of("UTC")).plusMonths(1).toString();
    String formattedDate = null;
    SimpleDateFormat inputFormat = new SimpleDateFormat(DATE_FORMAT);
    SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
    Date parsedDate;
    try
    {
      parsedDate = inputFormat.parse(date);
      formattedDate = outputFormat.format(parsedDate);
    }
    catch (ParseException e)
    {
      e.printStackTrace();
    }
    return formattedDate;
  }
}
