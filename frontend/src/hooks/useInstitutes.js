import { useQuery } from '@tanstack/react-query';
import { fetchInstitutes } from '../services/instituteService';

export function useInstitutes() {
  return useQuery(['institutes'], fetchInstitutes, { staleTime: 5 * 60 * 1000 });
}
