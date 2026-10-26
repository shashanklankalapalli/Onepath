import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { Layout } from './components/Layout';
import { ProtectedRoute } from './components/ProtectedRoute';

import { HomePage } from './pages/HomePage';
import { ServicesPage } from './pages/ServicesPage';
import { ServiceDetailPage } from './pages/ServiceDetailPage';
import { CollegeDashboardPage } from './pages/CollegeDashboardPage';
import { CollegeDetailPage } from './pages/CollegeDetailPage';
import { CollegeProcessDetailPage } from './pages/CollegeProcessDetailPage';
import { MyDocumentsPage } from './pages/MyDocumentsPage';
import { UpdatesPage } from './pages/UpdatesPage';
import { ServiceCentersPage } from './pages/ServiceCentersPage';
import { JosaaCounsellingPage } from './pages/JosaaCounsellingPage';
import { MarriageRegistrationPage } from './pages/MarriageRegistrationPage';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { ProfilePage } from './pages/ProfilePage';
import { ManageCollegePage } from './pages/ManageCollegePage';
import { ManageProcessPage } from './pages/ManageProcessPage';

export const App: React.FC = () => {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Layout />}>
            {/* Public Routes */}
            <Route index element={<HomePage />} />
            <Route path="services" element={<ServicesPage />} />
            <Route path="services/josaa-state-admissions-counselling" element={<JosaaCounsellingPage />} />
            <Route path="services/josaa-counselling" element={<JosaaCounsellingPage />} />
            <Route path="services/marriage-registration" element={<MarriageRegistrationPage />} />
            <Route path="services/marriage" element={<MarriageRegistrationPage />} />
            <Route path="services/:identifier" element={<ServiceDetailPage />} />
            <Route path="colleges" element={<CollegeDashboardPage />} />
            <Route path="colleges/:collegeId" element={<CollegeDetailPage />} />
            <Route path="colleges/:collegeId/processes/:processId" element={<CollegeProcessDetailPage />} />
            <Route path="updates" element={<UpdatesPage />} />
            <Route path="service-centers" element={<ServiceCentersPage />} />
            <Route path="service-centres" element={<ServiceCentersPage />} />
            <Route path="my-documents" element={<MyDocumentsPage />} />
            <Route path="login" element={<LoginPage />} />
            <Route path="register" element={<RegisterPage />} />

            {/* Authenticated Routes */}
            <Route element={<ProtectedRoute />}>
              <Route path="profile" element={<ProfilePage />} />
            </Route>

            {/* Admin Protected Routes */}
            <Route element={<ProtectedRoute allowedRoles={['COLLEGE_ADMIN', 'SUPER_ADMIN']} />}>
              <Route path="colleges/manage" element={<ManageCollegePage />} />
              <Route path="colleges/:collegeId/processes/new" element={<ManageProcessPage />} />
              <Route path="colleges/:collegeId/processes/:processId/edit" element={<ManageProcessPage />} />
            </Route>

            {/* 404 Catch All */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
};
export default App;
