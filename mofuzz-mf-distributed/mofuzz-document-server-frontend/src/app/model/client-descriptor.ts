export interface ClientDescriptor {
  id: string;
  name: string;
  description: string;

  notificationsDisabled: boolean;
  assignedExperiments: string[];
}
