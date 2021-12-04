-- phpMyAdmin SQL Dump
-- version 4.6.6deb5ubuntu0.5
-- https://www.phpmyadmin.net/
--
-- Host: localhost:3306
-- Generation Time: Dec 04, 2021 at 06:17 AM
-- Server version: 10.1.48-MariaDB-0ubuntu0.18.04.1
-- PHP Version: 7.2.24-0ubuntu0.18.04.10

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";

--
-- Database: `deebot`
--

-- --------------------------------------------------------

--
-- Table structure for table `achv_log`
--

CREATE TABLE `achv_log` (
                            `tgid` bigint(20) NOT NULL,
                            `achv` enum('FIRST_GAME','FIRST_WIN','PLAY_WITH_MINT','FIRST_BLOOD','ROOKIE','FAMILIARIZED','ADDICTED','AMATEUR','ADEPT','EXPERT','LOSE_IT_ALL','DEEP_FRIED','') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `bans`
--

CREATE TABLE `bans` (
                        `id` int(11) NOT NULL,
                        `tgid` bigint(20) DEFAULT NULL,
                        `until` int(11) NOT NULL,
                        `count` int(11) NOT NULL,
                        `type` enum('COMMAND','ADMIN') DEFAULT NULL,
                        `reason` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Table structure for table `games`
--

CREATE TABLE `games` (
                         `id` int(11) NOT NULL,
                         `gid` bigint(20) DEFAULT NULL,
                         `chips` int(11) DEFAULT NULL,
                         `game_sequence` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Triggers `games`
--
DELIMITER $$
CREATE TRIGGER `games_after_insert` AFTER INSERT ON `games` FOR EACH ROW BEGIN
    update groups set current_game=new.id where gid=new.gid;
END
    $$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `groups`
--

CREATE TABLE `groups` (
                          `gid` bigint(20) NOT NULL,
                          `chips_per_card` int(11) NOT NULL DEFAULT '1',
                          `wait_time` int(11) NOT NULL DEFAULT '120',
                          `turn_wait_time` int(11) NOT NULL DEFAULT '45',
                          `current_game` int(11) DEFAULT NULL,
                          `collect_place` bit(1) NOT NULL DEFAULT b'0',
                          `fry` bit(1) NOT NULL DEFAULT b'0',
                          `lang` enum('en','zh','hk') DEFAULT NULL,
                          `protest_mode` bit(1) NOT NULL DEFAULT b'0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `next_game`
--

CREATE TABLE `next_game` (
                             `tgid` bigint(20) NOT NULL,
                             `gid` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `tg_users`
--

CREATE TABLE `tg_users` (
                            `tgid` bigint(20) NOT NULL,
                            `username` varchar(32) DEFAULT NULL,
                            `chips` int(11) NOT NULL DEFAULT '2000',
                            `won_cards` int(11) NOT NULL DEFAULT '0',
                            `lost_cards` int(11) NOT NULL DEFAULT '0',
                            `game_count` int(11) NOT NULL DEFAULT '0',
                            `won_count` int(11) NOT NULL DEFAULT '0',
                            `current_game` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `achv_log`
--
ALTER TABLE `achv_log`
    ADD PRIMARY KEY (`tgid`,`achv`);

--
-- Indexes for table `bans`
--
ALTER TABLE `bans`
    ADD PRIMARY KEY (`id`);

--
-- Indexes for table `games`
--
ALTER TABLE `games`
    ADD PRIMARY KEY (`id`),
  ADD KEY `FK_games_groups` (`gid`);

--
-- Indexes for table `groups`
--
ALTER TABLE `groups`
    ADD PRIMARY KEY (`gid`),
  ADD KEY `FK_groups_games` (`current_game`);

--
-- Indexes for table `next_game`
--
ALTER TABLE `next_game`
    ADD PRIMARY KEY (`tgid`,`gid`);

--
-- Indexes for table `tg_users`
--
ALTER TABLE `tg_users`
    ADD PRIMARY KEY (`tgid`),
  ADD KEY `FK_tg_users_groups` (`current_game`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `bans`
--
ALTER TABLE `bans`
    MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=1322;
--
-- AUTO_INCREMENT for table `games`
--
ALTER TABLE `games`
    MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=318285;
--
-- Constraints for dumped tables
--

--
-- Constraints for table `games`
--
ALTER TABLE `games`
    ADD CONSTRAINT `FK_games_groups` FOREIGN KEY (`gid`) REFERENCES `groups` (`gid`);

--
-- Constraints for table `groups`
--
ALTER TABLE `groups`
    ADD CONSTRAINT `FK_groups_games` FOREIGN KEY (`current_game`) REFERENCES `games` (`id`);

--
-- Constraints for table `tg_users`
--
ALTER TABLE `tg_users`
    ADD CONSTRAINT `FK_tg_users_groups` FOREIGN KEY (`current_game`) REFERENCES `groups` (`gid`);
