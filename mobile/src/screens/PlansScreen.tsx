import React, { useEffect } from 'react';
import {
  View,
  Text,
  FlatList,
  StyleSheet,
  TouchableOpacity,
  ActivityIndicator,
} from 'react-native';
import { useAppDispatch, useAppSelector } from '@/store';
import { fetchPlans } from '@/store/slices/plansSlice';
import PlanCard from '@/components/molecules/PlanCard';

const PlansScreen: React.FC<{ navigation: any }> = ({ navigation }) => {
  const dispatch = useAppDispatch();
  const { plans, isLoading, error } = useAppSelector(state => state.plans);

  useEffect(() => {
    dispatch(fetchPlans());
  }, [dispatch]);

  const handleCreatePlan = () => {
    navigation.navigate('CreatePlan');
  };

  const handlePlanPress = (planId: number) => {
    navigation.navigate('PlanDetail', { planId });
  };

  if (isLoading && plans.length === 0) {
    return (
      <View style={styles.centered}>
        <ActivityIndicator size="large" color="#007AFF" />
      </View>
    );
  }

  if (error) {
    return (
      <View style={styles.centered}>
        <Text style={styles.errorText}>❌ {error}</Text>
        <TouchableOpacity style={styles.retryButton} onPress={() => dispatch(fetchPlans())}>
          <Text style={styles.retryText}>Retry</Text>
        </TouchableOpacity>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>My Travel Plans</Text>
        <TouchableOpacity style={styles.createButton} onPress={handleCreatePlan}>
          <Text style={styles.createButtonText}>+ New Plan</Text>
        </TouchableOpacity>
      </View>

      {plans.length === 0 ? (
        <View style={styles.empty}>
          <Text style={styles.emptyText}>No travel plans yet</Text>
          <Text style={styles.emptySubtext}>Create your first AI-powered travel plan!</Text>
          <TouchableOpacity style={styles.emptyButton} onPress={handleCreatePlan}>
            <Text style={styles.emptyButtonText}>Create Plan</Text>
          </TouchableOpacity>
        </View>
      ) : (
        <FlatList
          data={plans}
          keyExtractor={item => String(item.id)}
          renderItem={({ item }) => (
            <PlanCard plan={item} onPress={() => handlePlanPress(item.id)} />
          )}
          contentContainerStyle={styles.list}
          refreshing={isLoading}
          onRefresh={() => dispatch(fetchPlans())}
        />
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F5F5F5',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 16,
    backgroundColor: '#FFFFFF',
    borderBottomWidth: 1,
    borderBottomColor: '#E0E0E0',
  },
  title: {
    fontSize: 24,
    fontWeight: '700',
    color: '#333333',
  },
  createButton: {
    backgroundColor: '#007AFF',
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 20,
  },
  createButtonText: {
    color: '#FFFFFF',
    fontWeight: '600',
    fontSize: 14,
  },
  list: {
    padding: 16,
  },
  centered: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  errorText: {
    fontSize: 16,
    color: '#FF3B30',
    marginBottom: 16,
  },
  retryButton: {
    backgroundColor: '#007AFF',
    paddingHorizontal: 24,
    paddingVertical: 12,
    borderRadius: 8,
  },
  retryText: {
    color: '#FFFFFF',
    fontWeight: '600',
  },
  empty: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 32,
  },
  emptyText: {
    fontSize: 20,
    fontWeight: '600',
    color: '#333333',
    marginBottom: 8,
  },
  emptySubtext: {
    fontSize: 14,
    color: '#666666',
    marginBottom: 24,
    textAlign: 'center',
  },
  emptyButton: {
    backgroundColor: '#007AFF',
    paddingHorizontal: 32,
    paddingVertical: 14,
    borderRadius: 12,
  },
  emptyButtonText: {
    color: '#FFFFFF',
    fontWeight: '600',
    fontSize: 16,
  },
});

export default PlansScreen;

