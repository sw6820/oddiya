import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { TravelPlan } from '@/types';
import { format } from 'date-fns';

interface PlanCardProps {
  plan: TravelPlan;
  onPress: () => void;
}

const PlanCard: React.FC<PlanCardProps> = ({ plan, onPress }) => {
  const formatDate = (dateString: string) => {
    return format(new Date(dateString), 'MMM dd, yyyy');
  };

  const getDuration = () => {
    const start = new Date(plan.startDate);
    const end = new Date(plan.endDate);
    const days = Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24));
    return `${days} ${days === 1 ? 'day' : 'days'}`;
  };

  return (
    <TouchableOpacity style={styles.card} onPress={onPress} activeOpacity={0.7}>
      <View style={styles.header}>
        <Text style={styles.title}>{plan.title}</Text>
        <Text style={styles.duration}>{getDuration()}</Text>
      </View>
      
      <View style={styles.dateContainer}>
        <Text style={styles.dateLabel}>From:</Text>
        <Text style={styles.date}>{formatDate(plan.startDate)}</Text>
      </View>
      
      <View style={styles.dateContainer}>
        <Text style={styles.dateLabel}>To:</Text>
        <Text style={styles.date}>{formatDate(plan.endDate)}</Text>
      </View>
      
      {plan.details && plan.details.length > 0 && (
        <View style={styles.detailsContainer}>
          <Text style={styles.detailsLabel}>
            {plan.details.length} {plan.details.length === 1 ? 'activity' : 'activities'}
          </Text>
        </View>
      )}
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  card: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 16,
    marginBottom: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
    elevation: 3,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  title: {
    fontSize: 18,
    fontWeight: '700',
    color: '#333333',
    flex: 1,
  },
  duration: {
    fontSize: 14,
    fontWeight: '600',
    color: '#007AFF',
    backgroundColor: '#E3F2FD',
    paddingHorizontal: 12,
    paddingVertical: 4,
    borderRadius: 12,
  },
  dateContainer: {
    flexDirection: 'row',
    marginBottom: 6,
  },
  dateLabel: {
    fontSize: 14,
    color: '#666666',
    width: 50,
  },
  date: {
    fontSize: 14,
    color: '#333333',
    fontWeight: '500',
  },
  detailsContainer: {
    marginTop: 12,
    paddingTop: 12,
    borderTopWidth: 1,
    borderTopColor: '#F0F0F0',
  },
  detailsLabel: {
    fontSize: 13,
    color: '#666666',
  },
});

export default PlanCard;

