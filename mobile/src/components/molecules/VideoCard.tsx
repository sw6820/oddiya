import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Image } from 'react-native';
import { VideoJob } from '@/types';

interface VideoCardProps {
  video: VideoJob;
  onPress: () => void;
}

const VideoCard: React.FC<VideoCardProps> = ({ video, onPress }) => {
  const getStatusColor = (status: string) => {
    switch (status) {
      case 'COMPLETED':
        return '#4CAF50';
      case 'PROCESSING':
        return '#FF9800';
      case 'PENDING':
        return '#2196F3';
      case 'FAILED':
        return '#FF3B30';
      default:
        return '#999999';
    }
  };

  const getStatusText = (status: string) => {
    return status.charAt(0) + status.slice(1).toLowerCase();
  };

  return (
    <TouchableOpacity style={styles.card} onPress={onPress} activeOpacity={0.7}>
      <View style={styles.content}>
        {video.photoUrls && video.photoUrls.length > 0 && (
          <Image
            source={{ uri: video.photoUrls[0] }}
            style={styles.thumbnail}
            resizeMode="cover"
          />
        )}
        
        <View style={styles.info}>
          <View style={styles.header}>
            <Text style={styles.photoCount}>
              📷 {video.photoUrls?.length || 0} photos
            </Text>
            <View style={[styles.statusBadge, { backgroundColor: getStatusColor(video.status) }]}>
              <Text style={styles.statusText}>{getStatusText(video.status)}</Text>
            </View>
          </View>
          
          <Text style={styles.template}>Template: {video.template}</Text>
          
          {video.videoUrl && (
            <Text style={styles.ready}>✅ Video ready</Text>
          )}
        </View>
      </View>
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  card: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    marginBottom: 12,
    overflow: 'hidden',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
    elevation: 3,
  },
  content: {
    flexDirection: 'row',
  },
  thumbnail: {
    width: 120,
    height: 120,
    backgroundColor: '#F0F0F0',
  },
  info: {
    flex: 1,
    padding: 12,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  photoCount: {
    fontSize: 14,
    color: '#666666',
  },
  statusBadge: {
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 12,
  },
  statusText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#FFFFFF',
  },
  template: {
    fontSize: 13,
    color: '#666666',
    marginBottom: 4,
  },
  ready: {
    fontSize: 13,
    color: '#4CAF50',
    fontWeight: '600',
  },
});

export default VideoCard;

